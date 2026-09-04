package com.project.financeapi.dto.investment;

import com.project.financeapi.enumSystem.FixedIncomeStatus;
import com.project.financeapi.enumSystem.FixedIncomeType;
import com.project.financeapi.enumSystem.IndexerType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record FixedIncomeDashboardDTO(
        UUID investmentId,
        String name,
        FixedIncomeType type,
        IndexerType indexer,
        BigDecimal contractedRate,
        FixedIncomeStatus status,
        UUID accountId,
        String accountName,

        // Fotografia Financeira Atual (Soma matemática do livro-razão)
        BigDecimal totalPrincipal,   // Todo o dinheiro que o usuário colocou e ainda não tirou
        BigDecimal netBalance,       // O saldo total real de hoje (Principal + Rendimentos - Saques)
        BigDecimal totalProfit,      // Apenas para a UI pintar de verde (netBalance - totalPrincipal)

        LocalDate maturityDate,

        // Extrato completo para o front-end montar gráficos e histórico
        List<InvestmentTransactionDTO> history
) {}