package com.project.financeapi.dto.investment.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A linha do Livro-Razão. O extrato puro e imutável.
 */
public record TransactionLedgerDTO(
        UUID id,
        String type,
        LocalDate referenceDate,
        BigDecimal grossAmount,
        BigDecimal netAmount,
        BigDecimal irTaxRetained,
        BigDecimal iofTaxRetained,
        BigDecimal appliedMarketRate,
        String description
) {}