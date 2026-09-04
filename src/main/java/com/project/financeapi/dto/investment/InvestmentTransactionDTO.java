package com.project.financeapi.dto.investment;

import com.project.financeapi.enumSystem.InvestmentTransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InvestmentTransactionDTO(
        UUID transactionId,
        InvestmentTransactionType type,      // APPORT, DAILY_YIELD, RESCUE, ADJUSTMENT
        BigDecimal amount,                   // Valor líquido da movimentação (+ ou -)
        BigDecimal grossAmount,              // Lucro bruto (em caso de rendimento)
        BigDecimal irTax,                    // Imposto de Renda retido
        BigDecimal iofTax,                   // IOF retido
        BigDecimal b3CustodyFee,             // Taxa da B3 (se aplicável)
        LocalDate referenceDate,             // Data em que o evento ocorreu
        BigDecimal appliedMarketRate,        // A taxa do Bacen cravada no dia (Ex: 0.039270)
        String description                   // Ex: "Rendimento Diário", "Aporte Inicial"
) {}