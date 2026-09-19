package com.project.financeapi.service.yield.eligibility;

import com.project.financeapi.entity.InvestmentProduct;
import com.project.financeapi.entity.InvestmentTier;
import com.project.financeapi.repository.TransactionRepository;
import com.project.financeapi.service.yield.TierEligibilityRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class MinimumMonthlyMovementRule implements TierEligibilityRule {

    private final TransactionRepository transactionRepository;

    @Override
    public boolean isEligible(InvestmentTier tier, InvestmentProduct product, LocalDate processDate, BigDecimal balance) {
        if (tier.getRequiredMonthlyMovement() == null) {
            return true;
        }

        LocalDate start = processDate.withDayOfMonth(1);
        LocalDate end = processDate;

        BigDecimal totalMoved = transactionRepository.sumMovementsBetween(
                product.getAccount().getId(), start, end
        );

        return totalMoved.compareTo(tier.getRequiredMonthlyMovement()) >= 0;
    }
}
