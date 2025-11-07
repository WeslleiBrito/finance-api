package com.project.financeapi.dto.account.response;

import com.project.financeapi.enums.AccountStatus;
import com.project.financeapi.enums.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateInvestmentAccountResponseDTO(
        UUID id,
        String name,
        AccountType type,
        BigDecimal balance,
        AccountStatus status,
        BigDecimal riskLevel
) {
}
