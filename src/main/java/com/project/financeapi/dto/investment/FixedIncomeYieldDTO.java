package com.project.financeapi.dto.investment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FixedIncomeYieldDTO(
        UUID investmentId,
        String name,
        BigDecimal principalAmount,
        BigDecimal grossBalance,
        BigDecimal grossProfit,
        BigDecimal iofTaxAmount,
        BigDecimal irTaxAmount,
        BigDecimal b3CustodyFee,    // Novo campo de aprovisionamento
        BigDecimal netBalance,
        BigDecimal netProfit,
        LocalDate calculationDate,
        int calendarDaysElapsed,
        int businessDaysElapsed
) {}