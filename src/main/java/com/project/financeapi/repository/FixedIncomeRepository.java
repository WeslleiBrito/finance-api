package com.project.financeapi.repository;

import com.project.financeapi.entity.FixedIncome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FixedIncomeRepository extends JpaRepository<FixedIncome, UUID> {

    @Query("SELECT DISTINCT f FROM FixedIncome f " +
            "LEFT JOIN FETCH f.lots l " +
            "WHERE f.account.id = :accountId " +
            "ORDER BY f.maturityDate ASC")
    List<FixedIncome> findAllActiveByAccountIdWithLots(@Param("accountId") UUID accountId);

    @Query("SELECT f FROM FixedIncome f LEFT JOIN FETCH f.lots WHERE f.id = :id")
    Optional<FixedIncome> findByIdWithLots(@Param("id") UUID id);
}