package com.project.financeapi.repository;

import com.project.financeapi.entity.InvestmentProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface InvestmentProductRepository extends JpaRepository<InvestmentProduct, UUID> {
    @Query("SELECT DISTINCT p FROM InvestmentProduct p LEFT JOIN FETCH p.tiers WHERE p.account.id = :accountId")
    List<InvestmentProduct> findAllActiveByAccountIdWithDetails(@Param("accountId") UUID accountId);
}