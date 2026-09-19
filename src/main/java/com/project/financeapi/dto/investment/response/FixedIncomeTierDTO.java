package com.project.financeapi.dto.investment.response;

import java.math.BigDecimal;
import java.util.UUID;

public record FixedIncomeTierDTO(
        UUID id,
        BigDecimal minBalance,
        BigDecimal maxBalance,
        BigDecimal rateMultiplier
) {}