package com.project.financeapi.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TaxCalculatorUtil {

    // Tabela regressiva de IOF para os primeiros 29 dias (índice = dias corridos)
    private static final double[] IOF_RATES = {
            1.00, 0.96, 0.93, 0.90, 0.86, 0.83, 0.80, 0.76, 0.73, 0.70,
            0.66, 0.63, 0.60, 0.56, 0.53, 0.50, 0.46, 0.43, 0.40, 0.36,
            0.33, 0.30, 0.26, 0.23, 0.20, 0.16, 0.13, 0.10, 0.06, 0.03
    };

    /**
     * Calcula o valor do IOF sobre o lucro bruto baseado nos dias corridos.
     */
    public static BigDecimal calculateIOF(BigDecimal grossProfit, int calendarDays) {
        if (grossProfit.compareTo(BigDecimal.ZERO) <= 0 || calendarDays >= 30) {
            return BigDecimal.ZERO;
        }

        double rate = IOF_RATES[calendarDays];
        return grossProfit.multiply(BigDecimal.valueOf(rate)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula o Imposto de Renda Retido na Fonte (IRRF) baseado nos dias corridos.
     * @param baseAmount Valor base para cálculo (Lucro Bruto - IOF).
     * @param calendarDays Tempo do aporte até a data atual.
     * @param isTaxExempt Se true (ex: LCI/LCA), retorna zero.
     */
    public static BigDecimal calculateIR(BigDecimal baseAmount, int calendarDays, boolean isTaxExempt) {
        if (isTaxExempt || baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        double rate;
        if (calendarDays <= 180) {
            rate = 0.225; // 22,5%
        } else if (calendarDays <= 360) {
            rate = 0.200; // 20,0%
        } else if (calendarDays <= 720) {
            rate = 0.175; // 17,5%
        } else {
            rate = 0.150; // 15,0%
        }

        return baseAmount.multiply(BigDecimal.valueOf(rate)).setScale(2, RoundingMode.HALF_UP);
    }
}