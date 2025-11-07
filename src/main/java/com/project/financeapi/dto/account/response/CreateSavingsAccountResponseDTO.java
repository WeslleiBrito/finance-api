package com.project.financeapi.dto.account.response;

import com.project.financeapi.dto.account.create.CreateAccountBase;
import com.project.financeapi.enums.AccountStatus;
import com.project.financeapi.enums.AccountType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateSavingsAccountResponseDTO(
        UUID id,
        String name,
        AccountType type,
        BigDecimal balance,
        AccountStatus status,
        BigDecimal interestRate
) {
}
