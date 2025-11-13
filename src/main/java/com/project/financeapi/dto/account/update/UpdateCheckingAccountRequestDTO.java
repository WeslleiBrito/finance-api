package com.project.financeapi.dto.account.update;

import com.project.financeapi.dto.account.AccountUpdateDTO;
import com.project.financeapi.enums.AccountStatus;
import com.project.financeapi.enums.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateCheckingAccountRequestDTO(
    String name,
    AccountType type,
    AccountStatus status,
    UUID bankId,
    BigDecimal overdraftLimit
) implements AccountUpdateDTO {
}
