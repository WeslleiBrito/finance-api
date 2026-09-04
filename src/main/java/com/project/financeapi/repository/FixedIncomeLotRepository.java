package com.project.financeapi.repository;

import com.project.financeapi.entity.FixedIncomeLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface FixedIncomeLotRepository extends JpaRepository<FixedIncomeLot, UUID> {

    /**
     * Retorna os lotes ativos (com principal > 0) de uma sacola,
     * ordenados do MAIS ANTIGO para o MAIS NOVO (Regra PEPS/FIFO da Receita Federal).
     */
    @Query("""
        SELECT l FROM FixedIncomeLot l 
        WHERE l.fixedIncome.id = :fixedIncomeId 
          AND l.remainingPrincipal > 0 
        ORDER BY l.purchaseDate ASC
    """)
    List<FixedIncomeLot> findActiveLotsByFixedIncomeOrderByDateAsc(@Param("fixedIncomeId") UUID fixedIncomeId);

    List<FixedIncomeLot> findAllByRemainingPrincipalGreaterThan(BigDecimal zero);
}