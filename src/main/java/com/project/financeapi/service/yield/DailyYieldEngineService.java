package com.project.financeapi.service.yield;

import com.project.financeapi.entity.*;
import com.project.financeapi.enumSystem.InvestmentTransactionType;
import com.project.financeapi.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyYieldEngineService {

    private final InvestmentProductRepository productRepository;
    private final InvestmentTransactionRepository transactionRepository;
    private final MarketIndexRateRepository marketIndexRateRepository;
    private final HolidayRepository holidayRepository;
    private final YieldConversionStrategyFactory strategyFactory;
    private final List<TierEligibilityRule> eligibilityRules;

    @Transactional
    public void processPendingYieldsForAccount(UUID accountId) {
        log.info(">>> Iniciando processamento de rendimentos para a conta: {}", accountId);
        productRepository.findAllActiveByAccountIdWithDetails(accountId).forEach(this::processYieldForProduct);
        log.info("<<< Processamento finalizado para a conta: {}", accountId);
    }

    private void processYieldForProduct(InvestmentProduct product) {
        log.info("--- Avaliando Produto: {} (ID: {}) | Convenção: {} ---",
                product.getName(), product.getId(), product.getConvention());

        LocalDate endDate = LocalDate.now();

        List<FixedIncomeLot> allProductLots = product.getBoxes().stream()
                .flatMap(box -> box.getLots().stream()).toList();

        if (allProductLots.isEmpty() || product.getTiers().isEmpty()) {
            return;
        }

        LocalDate startDate = allProductLots.stream()
                .map(lot -> lot.getLastYieldDate().orElse(lot.getLedgerStartDate().minusDays(1)))
                .min(LocalDate::compareTo).orElse(endDate).plusDays(1);

        if (startDate.isAfter(endDate)) {
            log.info("Todos os rendimentos já estão em dia para este produto.");
            return;
        }

        YieldConversionStrategy strategy = strategyFactory.resolve(product.getConvention());
        List<InvestmentTransaction> newTransactions = new ArrayList<>();

        for (LocalDate loopDate = startDate; !loopDate.isAfter(endDate); loopDate = loopDate.plusDays(1)) {
            final LocalDate processDate = loopDate;

            if (isNonBusinessDay(processDate)) {
                log.debug("Dia {}: fim de semana/feriado. Pulando.", processDate);
                continue;
            }

            MarketIndexRate lastKnownRate = marketIndexRateRepository
                    .findFirstByIndexerTypeAndReferenceDateLessThanEqualOrderByReferenceDateDesc(product.getIndexer(), processDate)
                    .orElse(null);

            if (lastKnownRate == null) {
                log.warn("Dia {}: nenhuma taxa de mercado anterior conhecida. Pulando.", processDate);
                continue;
            }

            List<InvestmentTransaction> dayTransactions = product.requiresPerLotCalculation()
                    ? processPerLotYield(product, allProductLots, processDate, lastKnownRate, strategy)
                    : processAggregateYield(product, allProductLots, processDate, lastKnownRate, strategy);

            newTransactions.addAll(dayTransactions);
        }

        if (!newTransactions.isEmpty()) {
            log.info("Salvando {} novas transações de rendimento para o Produto {}.", newTransactions.size(), product.getName());
            transactionRepository.saveAll(newTransactions);
        }
    }

    private boolean isNonBusinessDay(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || holidayRepository.existsByDate(date);
    }

    /**
     * Fluxo original: agrega todos os lotes num "superBalance", aplica tiers
     * sobre o saldo total e rateia proporcionalmente entre os lotes.
     * Usado para CDI/Prefixado, onde não importa a data de compra de cada lote.
     */
    private List<InvestmentTransaction> processAggregateYield(
            InvestmentProduct product,
            List<FixedIncomeLot> allProductLots,
            LocalDate processDate,
            MarketIndexRate lastKnownRate,
            YieldConversionStrategy strategy) {

        BigDecimal superBalance = BigDecimal.ZERO;
        Map<FixedIncomeLot, BigDecimal> activeLotsEod = new HashMap<>();

        for (FixedIncomeLot lot : allProductLots) {
            if (hasYieldOnDate(lot, processDate)) continue;

            BigDecimal eodBalance = lot.projectStateUpTo(processDate).grossBalance();
            if (eodBalance.compareTo(BigDecimal.ZERO) > 0) {
                activeLotsEod.put(lot, eodBalance);
                superBalance = superBalance.add(eodBalance);
            }
        }

        if (superBalance.compareTo(BigDecimal.ZERO) <= 0) return List.of();

        // NOVO: cópia final, criada após o valor parar de mudar
        final BigDecimal finalSuperBalance = superBalance;

        BigDecimal dailyFraction = strategy.computeDailyFraction(lastKnownRate, processDate, null);
        if (dailyFraction.compareTo(BigDecimal.ZERO) == 0) return List.of();

        BigDecimal totalYieldForDay = BigDecimal.ZERO;

        for (InvestmentTier tier : product.getTiers()) {
            boolean eligible = eligibilityRules.stream()
                    .allMatch(rule -> rule.isEligible(tier, product, processDate, finalSuperBalance)); // usa a cópia

            if (!eligible) continue;

            BigDecimal min = tier.getMinBalance();
            BigDecimal max = tier.getMaxBalance() != null ? tier.getMaxBalance() : BigDecimal.valueOf(Long.MAX_VALUE);

            if (finalSuperBalance.compareTo(min) > 0) {
                BigDecimal slice = finalSuperBalance.min(max).subtract(min);
                BigDecimal tierMultiplier = tier.getRateMultiplier().divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
                BigDecimal sliceYield = slice.multiply(dailyFraction).multiply(tierMultiplier).setScale(4, RoundingMode.HALF_UP);
                totalYieldForDay = totalYieldForDay.add(sliceYield);
            }
        }

        if (totalYieldForDay.compareTo(BigDecimal.ZERO) <= 0) return List.of();

        log.debug("Dia {}: rendimento agregado sobre R$ {} = R$ {}", processDate, finalSuperBalance, totalYieldForDay);

        List<InvestmentTransaction> transactions = new ArrayList<>();
        for (Map.Entry<FixedIncomeLot, BigDecimal> entry : activeLotsEod.entrySet()) {
            FixedIncomeLot lot = entry.getKey();
            BigDecimal ratio = entry.getValue().divide(finalSuperBalance, 8, RoundingMode.HALF_UP);
            BigDecimal lotYield = totalYieldForDay.multiply(ratio).setScale(4, RoundingMode.HALF_UP);

            InvestmentTransaction tx = buildYieldTransaction(lot, processDate, lotYield, lastKnownRate.getRateValue());
            transactions.add(tx);
            lot.getTransactions().add(tx);
        }
        return transactions;
    }


    /**
     * Fluxo para convenções que dependem da data de compra do lote (poupança,
     * IPCA mensal): cada lote é avaliado isoladamente, sem ratear via
     * "superBalance", pois cada um pode estar em um dia de crédito diferente.
     */
    private List<InvestmentTransaction> processPerLotYield(
            InvestmentProduct product,
            List<FixedIncomeLot> allProductLots,
            LocalDate processDate,
            MarketIndexRate lastKnownRate,
            YieldConversionStrategy strategy) {

        List<InvestmentTransaction> transactions = new ArrayList<>();

        for (FixedIncomeLot lot : allProductLots) {
            if (hasYieldOnDate(lot, processDate)) continue;

            BigDecimal eodBalance = lot.projectStateUpTo(processDate).grossBalance();
            if (eodBalance.compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal fraction = strategy.computeDailyFraction(lastKnownRate, processDate, lot);
            if (fraction.compareTo(BigDecimal.ZERO) == 0) continue; // não é dia de crédito p/ este lote

            BigDecimal lotYield = BigDecimal.ZERO;
            for (InvestmentTier tier : product.getTiers()) {
                boolean eligible = eligibilityRules.stream()
                        .allMatch(rule -> rule.isEligible(tier, product, processDate, eodBalance));
                if (!eligible) continue;

                BigDecimal min = tier.getMinBalance();
                BigDecimal max = tier.getMaxBalance() != null ? tier.getMaxBalance() : BigDecimal.valueOf(Long.MAX_VALUE);

                if (eodBalance.compareTo(min) > 0) {
                    BigDecimal slice = eodBalance.min(max).subtract(min);
                    BigDecimal tierMultiplier = tier.getRateMultiplier().divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
                    lotYield = lotYield.add(slice.multiply(fraction).multiply(tierMultiplier).setScale(4, RoundingMode.HALF_UP));
                }
            }

            if (lotYield.compareTo(BigDecimal.ZERO) <= 0) continue;

            InvestmentTransaction tx = buildYieldTransaction(lot, processDate, lotYield, lastKnownRate.getRateValue());
            transactions.add(tx);
            lot.getTransactions().add(tx);
        }

        return transactions;
    }

    private boolean hasYieldOnDate(FixedIncomeLot lot, LocalDate date) {
        return lot.getTransactions().stream()
                .anyMatch(t -> t.getType() == InvestmentTransactionType.DAILY_YIELD && t.getReferenceDate().equals(date));
    }

    private InvestmentTransaction buildYieldTransaction(FixedIncomeLot lot, LocalDate date, BigDecimal amount, BigDecimal appliedRate) {
        return InvestmentTransaction.builder()
                .lot(lot)
                .type(InvestmentTransactionType.DAILY_YIELD)
                .referenceDate(date)
                .grossAmount(amount)
                .amount(amount)
                .irTax(BigDecimal.ZERO)
                .iofTax(BigDecimal.ZERO)
                .appliedMarketRate(appliedRate)
                .description("Rend. Diário Consolidado")
                .build();
    }
}
