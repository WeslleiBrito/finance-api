package com.project.financeapi.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreditCardSummaryDTO(
        BigDecimal globalLimit,
        BigDecimal globalAvailable,
        BigDecimal globalUsed,
        BestCardDTO bestCard,
        List<SnowballChartDTO> snowballChartData
) {
    public record BestCardDTO(String cardName, long daysToPay, LocalDate nextDue) {}
    public record SnowballChartDTO(String month, BigDecimal total) {}
}