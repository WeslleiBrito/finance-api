package com.project.financeapi.repository;

import com.project.financeapi.entity.FixedIncomeLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FixedIncomeLotRepository extends JpaRepository<FixedIncomeLot, UUID> {
    @Query("SELECT l FROM FixedIncomeLot l WHERE l.box.id = :boxId ORDER BY l.purchaseDate ASC")
    List<FixedIncomeLot> findActiveLotsForRescueByBox(@Param("boxId") UUID boxId);
}