package com.project.financeapi.dto.investment.response;

import com.project.financeapi.enumSystem.FixedIncomeStatus;
import com.project.financeapi.enumSystem.FixedIncomeType;
import com.project.financeapi.enumSystem.IndexerType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record FixedIncomeDashboardDTO(
        UUID id,
        String name,
        FixedIncomeType type, // <-- Atualizado: substituiu o Boolean isTaxExempt
        IndexerType indexer,
        BigDecimal contractedRate,
        LocalDate maturityDate,
        FixedIncomeStatus status,

        BigDecimal totalPrincipal,
        BigDecimal totalProjectedGrossBalance,
        BigDecimal totalProjectedTaxes,
        BigDecimal totalProjectedNetBalance,

        List<LotDetailDTO> activeLots,
        List<TransactionLedgerDTO> recentTransactions
) {}