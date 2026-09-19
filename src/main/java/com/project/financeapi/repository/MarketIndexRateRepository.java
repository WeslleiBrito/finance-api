package com.project.financeapi.repository;

import com.project.financeapi.entity.MarketIndexRate;
import com.project.financeapi.enumSystem.IndexerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarketIndexRateRepository extends JpaRepository<MarketIndexRate, UUID> {

    @Query("SELECT MAX(m.referenceDate) FROM MarketIndexRate m WHERE m.indexerType = :type")
    Optional<LocalDate> findMaxReferenceDateByIndexerType(@Param("type") IndexerType type);
    List<MarketIndexRate> findByIndexerTypeAndReferenceDateBetweenOrderByReferenceDateAsc(
            IndexerType indexerType,
            LocalDate startDate,
            LocalDate endDate
    );
    Optional<MarketIndexRate> findFirstByIndexerTypeAndReferenceDateLessThanEqualOrderByReferenceDateDesc(IndexerType indexer, LocalDate referenceDate);
}