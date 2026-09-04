package com.project.financeapi.service;

import com.project.financeapi.dto.investment.FixedIncomeDashboardDTO;
import com.project.financeapi.dto.investment.InvestmentTransactionDTO;
import com.project.financeapi.entity.FixedIncome;
import com.project.financeapi.entity.InvestmentTransaction;
import com.project.financeapi.repository.FixedIncomeRepository;
import com.project.financeapi.repository.InvestmentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvestmentLedgerService {

    private final FixedIncomeRepository fixedIncomeRepository;
    private final InvestmentTransactionRepository transactionRepository;

    /**
     * Retorna a visão detalhada de uma sacola específica, incluindo o extrato completo.
     */
    public FixedIncomeDashboardDTO getDashboard(UUID fixedIncomeId) {
        FixedIncome investment = fixedIncomeRepository.findById(fixedIncomeId)
                .orElseThrow(() -> new IllegalArgumentException("Sacola não encontrada."));

        // 1. Cálculos consolidados via banco de dados (O(1) na aplicação)
        BigDecimal netBalance = transactionRepository.calculateNetBalanceByFixedIncome(fixedIncomeId);

        // O principal total que ainda está rendendo é a soma do 'remainingPrincipal' de todos os lotes ativos
        BigDecimal totalPrincipal = investment.getLots().stream()
                .map(lot -> lot.getRemainingPrincipal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // O lucro líquido atual para a interface pintar de verde (Saldo atual - Dinheiro injetado)
        BigDecimal totalProfit = netBalance.subtract(totalPrincipal).max(BigDecimal.ZERO);

        // 2. Busca o extrato de transações para desenhar o gráfico
        List<InvestmentTransaction> history = transactionRepository.findAllByFixedIncomeIdOrderByDateDesc(fixedIncomeId);
        List<InvestmentTransactionDTO> historyDTOs = history.stream()
                .map(this::toTransactionDTO)
                .toList();

        return new FixedIncomeDashboardDTO(
                investment.getId(),
                investment.getName(),
                investment.getType(),
                investment.getIndexer(),
                investment.getContractedRate(),
                investment.getStatus(),
                investment.getAccount().getId(),
                investment.getAccount().getName(),
                totalPrincipal,
                netBalance,
                totalProfit,
                investment.getMaturityDate(),
                historyDTOs
        );
    }

    /**
     * Retorna um resumo de todas as sacolas ativas de uma conta.
     * Útil para a tela principal da conta corrente onde lista todos os investimentos.
     */
    public List<FixedIncomeDashboardDTO> getAllActiveDashboardsByAccount(UUID accountId) {
        List<FixedIncome> activeInvestments = fixedIncomeRepository.findAllByAccountIdAndStatus(
                accountId,
                com.project.financeapi.enumSystem.FixedIncomeStatus.ACTIVE
        );

        // Mapeia cada sacola para o seu respectivo Dashboard
        return activeInvestments.stream()
                .map(inv -> getDashboard(inv.getId()))
                .toList();
    }

    private InvestmentTransactionDTO toTransactionDTO(InvestmentTransaction tx) {
        return new InvestmentTransactionDTO(
                tx.getId(),
                tx.getType(),
                tx.getAmount(),
                tx.getGrossAmount(),
                tx.getIrTax(),
                tx.getIofTax(),
                tx.getB3CustodyFee(),
                tx.getReferenceDate(),
                tx.getAppliedMarketRate(),
                tx.getDescription()
        );
    }
}