package com.project.financeapi.repository;

import com.project.financeapi.entity.FixedIncomeLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FixedIncomeLotRepository extends JpaRepository<FixedIncomeLot, UUID> {

    @Query("SELECT DISTINCT l FROM FixedIncomeLot l " +
            "JOIN FETCH l.fixedIncome f " +
            "WHERE f.account.id = :accountId")
    List<FixedIncomeLot> findActiveLotsByAccountId(@Param("accountId") UUID accountId);

    @Query("SELECT DISTINCT l FROM FixedIncomeLot l " +
            "WHERE l.fixedIncome.id = :fixedIncomeId " +
            "ORDER BY l.purchaseDate ASC")
    List<FixedIncomeLot> findActiveLotsForRescueOderByOldest(@Param("fixedIncomeId") UUID fixedIncomeId);
}