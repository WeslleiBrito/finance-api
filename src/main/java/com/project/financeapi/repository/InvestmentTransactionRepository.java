package com.project.financeapi.repository;

import com.project.financeapi.entity.InvestmentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface InvestmentTransactionRepository extends JpaRepository<InvestmentTransaction, UUID> {

    /**
     * Busca o extrato completo de uma sacola (todos os lotes), ordenado pela data.
     * Útil para plotar o gráfico de evolução patrimonial no Front-end.
     */
    @Query("""
        SELECT t FROM InvestmentTransaction t 
        WHERE t.lot.fixedIncome.id = :fixedIncomeId 
        ORDER BY t.referenceDate DESC, t.id DESC
    """)
    List<InvestmentTransaction> findAllByFixedIncomeIdOrderByDateDesc(@Param("fixedIncomeId") UUID fixedIncomeId);

    /**
     * O consolidado real: Soma todas as transações (Aportes + Rendimentos - Resgates)
     * para descobrir o Saldo Líquido atual (netBalance) da sacola inteira.
     */
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) 
        FROM InvestmentTransaction t 
        WHERE t.lot.fixedIncome.id = :fixedIncomeId
    """)
    BigDecimal calculateNetBalanceByFixedIncome(@Param("fixedIncomeId") UUID fixedIncomeId);

    /**
     * Descobre o lucro bruto total gerado por esta sacola até hoje.
     * Soma apenas a coluna grossAmount das transações do tipo DAILY_YIELD.
     */
    @Query("""
        SELECT COALESCE(SUM(t.grossAmount), 0) 
        FROM InvestmentTransaction t 
        WHERE t.lot.fixedIncome.id = :fixedIncomeId 
          AND t.type = 'DAILY_YIELD'
    """)
    BigDecimal calculateTotalGrossProfitByFixedIncome(@Param("fixedIncomeId") UUID fixedIncomeId);

    /**
     * Descobre o total de Imposto de Renda já retido / provisionado na sacola.
     */
    @Query("""
        SELECT COALESCE(SUM(t.irTax), 0) 
        FROM InvestmentTransaction t 
        WHERE t.lot.fixedIncome.id = :fixedIncomeId
    """)
    BigDecimal calculateTotalIrTaxByFixedIncome(@Param("fixedIncomeId") UUID fixedIncomeId);

    /**
     * Retorna o Saldo Líquido atual de um Lote ESPECÍFICO (útil no momento do resgate).
     */
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) 
        FROM InvestmentTransaction t 
        WHERE t.lot.id = :lotId
    """)
    BigDecimal calculateNetBalanceByLot(@Param("lotId") UUID lotId);

    /**
     * Descobre o lucro bruto total gerado exclusivamente por um Lote específico.
     */
    @Query("""
        SELECT COALESCE(SUM(t.grossAmount), 0) 
        FROM InvestmentTransaction t 
        WHERE t.lot.id = :lotId 
          AND t.type = 'DAILY_YIELD'
    """)
    BigDecimal calculateTotalGrossProfitByLot(@Param("lotId") UUID lotId);

    /**
     * Descobre o total de IOF já retido / provisionado em um Lote específico.
     */
    @Query("""
        SELECT COALESCE(SUM(t.iofTax), 0) 
        FROM InvestmentTransaction t 
        WHERE t.lot.id = :lotId
    """)
    BigDecimal calculateTotalIofTaxByLot(@Param("lotId") UUID lotId);

    /**
     * Descobre o total de Imposto de Renda já retido / provisionado em um Lote específico.
     */
    @Query("""
        SELECT COALESCE(SUM(t.irTax), 0) 
        FROM InvestmentTransaction t 
        WHERE t.lot.id = :lotId
    """)
    BigDecimal calculateTotalIrTaxByLot(@Param("lotId") UUID lotId);
}