package com.project.financeapi.service.yield.strategy;

import com.project.financeapi.entity.FixedIncomeLot;
import com.project.financeapi.entity.MarketIndexRate;
import com.project.financeapi.enumSystem.YieldConvention;
import com.project.financeapi.service.yield.YieldConversionStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Component
public class CdiExponentialStrategy implements YieldConversionStrategy {

    @Override
    public YieldConvention supports() {
        return YieldConvention.CDI_EXPONENTIAL_252;
    }

    @Override
    public BigDecimal computeDailyFraction(MarketIndexRate rate, LocalDate processDate, FixedIncomeLot lot) {
        // rate.getRateValue() já é a taxa DI DIÁRIA (ex: 0,05166 = 0,05166%/dia)
        // Não é necessário nenhuma conversão exponencial anual→diária.
        return rate.getRateValue().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
    }
}
