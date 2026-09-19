package com.project.financeapi.service.yield.strategy;

import com.project.financeapi.entity.FixedIncomeLot;
import com.project.financeapi.entity.MarketIndexRate;
import com.project.financeapi.enumSystem.YieldConvention;
import com.project.financeapi.service.yield.YieldConversionStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * IPCA+: credita 1x por mês (normalmente no dia correspondente à compra,
 * ou no fechamento do mês, dependendo da instituição). Aqui assumimos
 * crédito no dia de aniversário do lote, igual poupança, mas com taxa
 * já cheia do período (pré + IPCA do mês, informada em MarketIndexRate).
 *
 * ATENÇÃO: como depende do dia de compra do LOTE, esta convenção também
 * exige cálculo por-lote (ver FixedIncomeLot.requiresPerLotCalculation()).
 */
@Component
public class IpcaMonthlyCapitalizedStrategy implements YieldConversionStrategy {

    @Override
    public YieldConvention supports() {
        return YieldConvention.IPCA_MONTHLY_CAPITALIZED;
    }

    @Override
    public BigDecimal computeDailyFraction(MarketIndexRate rate, LocalDate processDate, FixedIncomeLot lot) {
        if (lot == null || processDate.getDayOfMonth() != lot.getPurchaseDate().getDayOfMonth()) {
            return BigDecimal.ZERO;
        }
        // rate.getRateValue() aqui já representa a taxa cheia do período (mensal),
        // não uma taxa anual a ser convertida.
        return rate.getRateValue().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
    }
}
