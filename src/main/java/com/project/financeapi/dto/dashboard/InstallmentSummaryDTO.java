package com.project.financeapi.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record InstallmentSummaryDTO(
        BigDecimal totalPaid,
        BigDecimal totalThisMonth,
        BigDecimal totalOverdue,
        BigDecimal totalOpen,
        List<MonthlyChartDTO> chartData
) {
    public record MonthlyChartDTO(String name, BigDecimal total) {}
}