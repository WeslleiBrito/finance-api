package com.project.financeapi.dto.investment.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BoxDetailDTO(
        UUID id,
        String name,
        BigDecimal totalPrincipal,
        BigDecimal totalGrossBalance,
        BigDecimal totalTaxes,
        BigDecimal totalNetBalance,
        List<LotDetailDTO> activeLots
) {}
