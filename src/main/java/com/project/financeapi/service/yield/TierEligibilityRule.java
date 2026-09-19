package com.project.financeapi.service.yield;

import com.project.financeapi.entity.InvestmentProduct;
import com.project.financeapi.entity.InvestmentTier;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TierEligibilityRule {
    boolean isEligible(InvestmentTier tier, InvestmentProduct product, LocalDate processDate, BigDecimal superBalance);
}
