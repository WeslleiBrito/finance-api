package com.project.financeapi.dto.investment.request;

import java.math.BigDecimal;

public record TierRequestDTO(
        BigDecimal minBalance,
        BigDecimal maxBalance,
        BigDecimal rateMultiplier,
        BigDecimal requiredMonthlyMovement // NOVO: null = sem exigência de movimentação
) {}
