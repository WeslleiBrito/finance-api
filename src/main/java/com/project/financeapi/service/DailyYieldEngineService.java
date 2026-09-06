package com.project.financeapi.service;

import com.project.financeapi.entity.FixedIncomeLot;
import com.project.financeapi.entity.InvestmentTransaction;
import com.project.financeapi.entity.MarketIndexRate;
import com.project.financeapi.enumSystem.IndexerType;
import com.project.financeapi.enumSystem.InvestmentTransactionType;
import com.project.financeapi.repository.FixedIncomeLotRepository;
import com.project.financeapi.repository.InvestmentTransactionRepository;
import com.project.financeapi.repository.MarketIndexRateRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DailyYieldEngineService {

    private static final Logger log = LoggerFactory.getLogger(DailyYieldEngineService.class);

    private final FixedIncomeLotRepository lotRepository;
    private final InvestmentTransactionRepository transactionRepository;
    private final MarketIndexRateRepository marketIndexRateRepository;

    @Transactional
    public void processPendingYieldsForAccount(UUID accountId) {
        List<FixedIncomeLot> allLots = lotRepository.findActiveLotsByAccountId(accountId);
        for (FixedIncomeLot lot : allLots) {
            // Delegação ao Event Sourcing: O filtro de "Ativo" ocorre validando o Saldo na memória
            if (lot.projectState().grossBalance().compareTo(BigDecimal.ZERO) > 0) {
                processYieldForLot(lot);
            }
        }
    }

    @Transactional
    public void processPendingYieldsForFixedIncome(UUID fixedIncomeId) {
        List<FixedIncomeLot> allLots = lotRepository.findActiveLotsForRescueOderByOldest(fixedIncomeId);
        for (FixedIncomeLot lot : allLots) {
            if (lot.projectState().grossBalance().compareTo(BigDecimal.ZERO) > 0) {
                processYieldForLot(lot);
            }
        }
    }

    private void processYieldForLot(FixedIncomeLot lot) {
        LocalDate endDate = LocalDate.now().minusDays(1);

        // O motor pergunta a data do último juro diretamente para os eventos do Lote
        LocalDate lastYieldDate = lot.getLastYieldDate()
                .orElse(lot.getLedgerStartDate().minusDays(1));

        LocalDate startDate = lastYieldDate.plusDays(1);

        if (startDate.isAfter(endDate)) {
            return;
        }

        log.debug("Processando juros para o Lote {} de {} até {}", lot.getId(), startDate, endDate);

        // A BASE DE CÁLCULO histórica é obtida viajando no tempo até o 'lastYieldDate'
        BigDecimal currentGrossBalance = lot.projectStateUpTo(lastYieldDate).grossBalance();

        if (currentGrossBalance.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        IndexerType indexer = lot.getFixedIncome().getIndexer();
        List<MarketIndexRate> marketRates = marketIndexRateRepository
                .findByIndexerTypeAndReferenceDateBetweenOrderByReferenceDateAsc(indexer, startDate, endDate);

        if (marketRates.isEmpty()) return;

        BigDecimal contractedRate = lot.getFixedIncome().getContractedRate();
        List<InvestmentTransaction> newYieldTransactions = new ArrayList<>();

        for (MarketIndexRate marketRate : marketRates) {
            BigDecimal baseMarketRate = marketRate.getRateValue().divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
            BigDecimal contractMultiplier = contractedRate.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
            BigDecimal dailyMultiplier = baseMarketRate.multiply(contractMultiplier);

            BigDecimal dailyYield = currentGrossBalance.multiply(dailyMultiplier).setScale(4, RoundingMode.HALF_UP);

            currentGrossBalance = currentGrossBalance.add(dailyYield);

            InvestmentTransaction yieldTransaction = InvestmentTransaction.builder()
                    .lot(lot)
                    .type(InvestmentTransactionType.DAILY_YIELD)
                    .referenceDate(marketRate.getReferenceDate())
                    .grossAmount(dailyYield)
                    .amount(dailyYield)
                    .irTax(BigDecimal.ZERO)
                    .iofTax(BigDecimal.ZERO)
                    .appliedMarketRate(marketRate.getRateValue())
                    .description("Rendimento Diário (" + indexer.name() + ")")
                    .build();

            newYieldTransactions.add(yieldTransaction);
        }

        transactionRepository.saveAll(newYieldTransactions);
    }
}