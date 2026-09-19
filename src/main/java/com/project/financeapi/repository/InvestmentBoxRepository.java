package com.project.financeapi.repository;

import com.project.financeapi.entity.InvestmentBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface InvestmentBoxRepository extends JpaRepository<InvestmentBox, UUID> {
    @Query("SELECT b FROM InvestmentBox b JOIN FETCH b.product p LEFT JOIN FETCH b.lots l WHERE b.id = :boxId")
    Optional<InvestmentBox> findByIdWithDetails(@Param("boxId") UUID boxId);
}