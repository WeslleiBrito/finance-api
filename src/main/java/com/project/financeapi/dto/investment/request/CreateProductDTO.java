package com.project.financeapi.dto.investment.request;

import com.project.financeapi.enumSystem.FixedIncomeType;
import com.project.financeapi.enumSystem.IndexerType;
import com.project.financeapi.enumSystem.YieldConvention;
import java.util.List;
import java.util.UUID;

public record CreateProductDTO(
        UUID accountId,
        String name,
        IndexerType indexer,
        FixedIncomeType type,
        YieldConvention convention, // NOVO: define a matemática de conversão de taxa
        List<TierRequestDTO> tiers
) {}
