package com.project.financeapi.dto.account.response;

import com.project.financeapi.dto.account.AccountResponseDTO;
import com.project.financeapi.enumSystem.AccountStatus;
import com.project.financeapi.enumSystem.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateCheckingAccountResponseDTO(
        UUID id,
        String name,
        AccountType type,
        BigDecimal balance,
        AccountStatus status,
        BigDecimal overdraftLimit
) implements AccountResponseDTO{}
