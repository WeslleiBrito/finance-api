package com.project.financeapi.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryDTO(
        BigDecimal totalBalance,
        BigDecimal toReceive,
        BigDecimal toPay,
        long dueSoonCount,
        List<CategoryTotalDTO> outflowByCategory,
        List<InstrumentTotalDTO> outflowByInstrument,
        List<CategoryTotalDTO> inflowByCategory,
        List<InstrumentTotalDTO> inflowByInstrument,
        List<MonthlyChartDTO> chartData
) {
    public record CategoryTotalDTO(String name, BigDecimal value) {}
    public record InstrumentTotalDTO(String name, BigDecimal value) {}
    public record MonthlyChartDTO(String month, BigDecimal entradas, BigDecimal saidas) {}
}