package com.project.financeapi.service.yield;

import com.project.financeapi.entity.FixedIncomeLot;
import com.project.financeapi.entity.MarketIndexRate;
import com.project.financeapi.enumSystem.YieldConvention;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface YieldConversionStrategy {

    YieldConvention supports();

    /**
     * Retorna a fração da taxa a aplicar HOJE sobre o saldo do lote/produto.
     * Pode retornar ZERO em dias que não creditam rendimento nesta convenção
     * (ex: poupança fora da data de aniversário do aporte).
     *
     * @param rate        última taxa de mercado conhecida (Bacen/B3)
     * @param processDate data sendo processada
     * @param lot         lote de referência (necessário para convenções por-lote,
     *                    como poupança-aniversário; pode ser null para convenções agregadas)
     */
    BigDecimal computeDailyFraction(MarketIndexRate rate, LocalDate processDate, FixedIncomeLot lot);
}
