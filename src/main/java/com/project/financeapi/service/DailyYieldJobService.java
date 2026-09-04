package com.project.financeapi.service;

import com.project.financeapi.entity.FixedIncomeLot;
import com.project.financeapi.entity.InvestmentTransaction;
import com.project.financeapi.entity.MarketIndex;
import com.project.financeapi.enumSystem.IndexType;
import com.project.financeapi.enumSystem.InvestmentTransactionType;
import com.project.financeapi.repository.FixedIncomeLotRepository;
import com.project.financeapi.repository.InvestmentTransactionRepository;
import com.project.financeapi.repository.MarketIndexRepository;
import com.project.financeapi.util.TaxCalculatorUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyYieldJobService {

    private final FixedIncomeLotRepository lotRepository;
    private final InvestmentTransactionRepository transactionRepository;
    private final MarketIndexRepository marketIndexRepository;
    private final BusinessDayService businessDayService;

    /**
     * Roda todos os dias às 02:00 da manhã.
     */
    @Scheduled(cron = "0 */2 * * * *")
    @Transactional
    public void processDailyYields() {
        LocalDate referenceDate = LocalDate.now().minusDays(1);

        if (!businessDayService.isBusinessDay(referenceDate)) {
            log.info("Data {} não é dia útil. Rendimentos não processados.", referenceDate);
            return;
        }

        log.info("Iniciando processamento de rendimentos para a data base: {}", referenceDate);

        // Utiliza a assinatura existente no repositório passando a mesma data nos dois parâmetros
        List<MarketIndex> dailyIndices = marketIndexRepository.findByIndexTypeAndReferenceDateBetween(
                IndexType.CDI, referenceDate, referenceDate
        );

        // Busca a taxa CDI exata do dia anterior
        BigDecimal cdiRate = marketIndexRepository.findByIndexTypeAndReferenceDate(IndexType.CDI, referenceDate)
                .map(MarketIndex::getRate)
                .orElse(BigDecimal.ZERO);

        if (cdiRate.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("Taxa CDI não encontrada para a data {}. O processamento pode ficar defasado.", referenceDate);
            return;
        }

        // Busca todos os lotes que ainda possuem dinheiro rendendo
        List<FixedIncomeLot> activeLots = lotRepository.findAllByRemainingPrincipalGreaterThan(BigDecimal.ZERO);

        for (FixedIncomeLot lot : activeLots) {
            try {
                processSingleLot(lot, referenceDate, cdiRate);
            } catch (Exception e) {
                log.error("Erro ao processar rendimento do lote {}: {}", lot.getId(), e.getMessage());
            }
        }

        log.info("Processamento de rendimentos concluído com sucesso.");
    }

    private void processSingleLot(FixedIncomeLot lot, LocalDate referenceDate, BigDecimal dailyCdiRate) {
        if (!referenceDate.isAfter(lot.getPurchaseDate())) return;

        // 1. Apura o Saldo Bruto Atual do Lote
        BigDecimal currentGrossBalance = lot.getRemainingPrincipal()
                .add(transactionRepository.calculateTotalGrossProfitByLot(lot.getId()));

        // 2. Calcula o Fator de Rendimento Diário
        BigDecimal baseRate = dailyCdiRate.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        BigDecimal contractMultiplier = lot.getFixedIncome().getContractedRate()
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        BigDecimal dailyFactor = BigDecimal.ONE.add(baseRate.multiply(contractMultiplier));

        // 3. Calcula o Lucro Bruto Exclusivo deste dia
        BigDecimal newGrossBalance = currentGrossBalance.multiply(dailyFactor).setScale(4, RoundingMode.HALF_UP);
        BigDecimal dailyGrossYield = newGrossBalance.subtract(currentGrossBalance);

        if (dailyGrossYield.compareTo(BigDecimal.ZERO) <= 0) return;

        // 4. Lógica de Deltas para Impostos Provisionados
        int calendarDays = (int) ChronoUnit.DAYS.between(lot.getPurchaseDate(), referenceDate);
        BigDecimal totalAccumulatedGrossProfit = newGrossBalance.subtract(lot.getRemainingPrincipal());

        BigDecimal newTotalIof = TaxCalculatorUtil.calculateIOF(totalAccumulatedGrossProfit, calendarDays);
        BigDecimal baseIR = totalAccumulatedGrossProfit.subtract(newTotalIof).max(BigDecimal.ZERO);
        BigDecimal newTotalIr = TaxCalculatorUtil.calculateIR(baseIR, calendarDays, lot.getFixedIncome().getType().isTaxExempt());

        BigDecimal alreadyProvisionedIof = transactionRepository.calculateTotalIofTaxByLot(lot.getId());
        BigDecimal alreadyProvisionedIr = transactionRepository.calculateTotalIrTaxByLot(lot.getId());

        BigDecimal deltaIof = newTotalIof.subtract(alreadyProvisionedIof);
        BigDecimal deltaIr = newTotalIr.subtract(alreadyProvisionedIr);

        // 5. O Valor Líquido que entra na sacola
        BigDecimal netDailyYield = dailyGrossYield.subtract(deltaIof).subtract(deltaIr);

        // 6. Grava a Transação Imutável no Livro-Razão
        InvestmentTransaction yieldTx = new InvestmentTransaction();
        yieldTx.setLot(lot);
        yieldTx.setType(InvestmentTransactionType.DAILY_YIELD);
        yieldTx.setAmount(netDailyYield);
        yieldTx.setGrossAmount(dailyGrossYield);
        yieldTx.setIofTax(deltaIof);
        yieldTx.setIrTax(deltaIr);
        yieldTx.setB3CustodyFee(BigDecimal.ZERO);
        yieldTx.setReferenceDate(referenceDate);
        yieldTx.setAppliedMarketRate(dailyCdiRate);
        yieldTx.setDescription("Rendimento Diário");

        transactionRepository.save(yieldTx);
    }
}