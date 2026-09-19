package com.project.financeapi.service.yield.strategy;

import com.project.financeapi.entity.FixedIncomeLot;
import com.project.financeapi.entity.MarketIndexRate;
import com.project.financeapi.enumSystem.YieldConvention;
import com.project.financeapi.service.yield.YieldConversionStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

// Mesma matemática do CDI (exponencial base 252), mas semanticamente
// separada porque prefixado NÃO acompanha o mercado dia a dia — a taxa
// contratada fica fixa desde a compra. Mantido como classe própria para
// não acoplar as duas regras de negócio no futuro (ex: se um dia CDI
// mudar para base 360, prefixado não deve ser afetado).
@Component
public class PrefixadoExponentialStrategy implements YieldConversionStrategy {

    private static final int BUSINESS_DAYS_YEAR = 252;

    @Override
    public YieldConvention supports() {
        return YieldConvention.PREFIXADO_EXPONENTIAL_252;
    }

    @Override
    public BigDecimal computeDailyFraction(MarketIndexRate rate, LocalDate processDate, FixedIncomeLot lot) {
        BigDecimal annual = rate.getRateValue().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        double daily = Math.pow(1 + annual.doubleValue(), 1.0 / BUSINESS_DAYS_YEAR) - 1;
        return BigDecimal.valueOf(daily).setScale(10, RoundingMode.HALF_UP);
    }
}
