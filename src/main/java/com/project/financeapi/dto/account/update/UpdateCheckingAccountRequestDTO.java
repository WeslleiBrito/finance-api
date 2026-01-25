package com.project.financeapi.dto.account.update;

import com.project.financeapi.dto.account.AccountUpdateDTO;
import com.project.financeapi.enumSystem.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateCheckingAccountRequestDTO(
    String name,
    AccountType type,
    UUID bankId,
    BigDecimal overdraftLimit
) implements AccountUpdateDTO {
}
