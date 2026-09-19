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
public class PoupancaAniversarioStrategy implements YieldConversionStrategy {

    @Override
    public YieldConvention supports() {
        return YieldConvention.POUPANCA_MONTHLY_ANIVERSARIO;
    }

    @Override
    public BigDecimal computeDailyFraction(MarketIndexRate rate, LocalDate processDate, FixedIncomeLot lot) {
        if (lot == null || processDate.getDayOfMonth() != lot.getPurchaseDate().getDayOfMonth()) {
            return BigDecimal.ZERO;
        }
        return rate.getRateValue().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
    }
}
