package com.project.financeapi.dto.investment.response;

import com.project.financeapi.enumSystem.FixedIncomeStatus;
import com.project.financeapi.enumSystem.FixedIncomeType;
import com.project.financeapi.enumSystem.IndexerType;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductDashboardDTO(
        UUID id,
        String name,
        FixedIncomeType type,
        IndexerType indexer,
        BigDecimal displayRate,
        FixedIncomeStatus status,
        BigDecimal totalProductBalance,
        List<FixedIncomeTierDTO> tiers,
        List<BoxDetailDTO> boxes
) {}