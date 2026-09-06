package com.project.financeapi.dto.investment.response;

import com.project.financeapi.enumSystem.InvestmentTransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A linha do Livro-Razão. O extrato puro e imutável.
 */
public record TransactionLedgerDTO(
        UUID id,
        InvestmentTransactionType type, // APPORT, DAILY_YIELD, RESCUE, LIQUIDATION
        LocalDate referenceDate,

        BigDecimal grossAmount, // O volume movimentado/rendido
        BigDecimal netAmount, // Só tem valor diferente do gross no Resgate/Liquidação

        BigDecimal irTaxRetained, // Imposto materializado (só no resgate)
        BigDecimal iofTaxRetained, // Imposto materializado (só no resgate)

        BigDecimal appliedMarketRate, // Taxa Bacen do dia para auditoria (nula em aportes)
        String description
) {}