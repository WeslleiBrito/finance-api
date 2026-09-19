package com.project.financeapi.repository;

import com.project.financeapi.entity.InvestmentTier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface InvestmentTierRepository extends JpaRepository<InvestmentTier, UUID> {
}