package com.project.financeapi.dto.account.update;

import com.project.financeapi.dto.account.AccountUpdateDTO;

import com.project.financeapi.enumSystem.AccountStatus;
import com.project.financeapi.enumSystem.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateInvestmentAccountRequestDTO(
        String name,
        AccountType type,
        AccountStatus status,
        UUID bankId,
        BigDecimal riskLevel
) implements AccountUpdateDTO {
}
