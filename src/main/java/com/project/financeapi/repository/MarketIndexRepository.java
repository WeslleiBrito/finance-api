package com.project.financeapi.repository;

import com.project.financeapi.entity.MarketIndex;
import com.project.financeapi.enumSystem.IndexType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarketIndexRepository extends JpaRepository<MarketIndex, Long> {

    // Usado pelo BacenSyncService para saber onde parou
    @Query("SELECT MAX(m.referenceDate) FROM MarketIndex m WHERE m.indexType = :type")
    Optional<LocalDate> findMaxDateByType(@Param("type") IndexType type);

    // 1. Usado pelo YieldCalculatorService (Busca um período inteiro)
    List<MarketIndex> findByIndexTypeAndReferenceDateBetween(
            IndexType indexType,
            LocalDate startDate,
            LocalDate endDate
    );

    // 2. Usado pelo DailyYieldJobService (Busca um dia exato)
    Optional<MarketIndex> findByIndexTypeAndReferenceDate(
            IndexType indexType,
            LocalDate referenceDate
    );
}