package com.project.financeapi.dto.account;

import com.project.financeapi.enums.AccountStatus;
import com.project.financeapi.enums.AccountType;

public record UpdateAccountRequestDTO(
        String name,
        AccountType type,
        AccountStatus status
) {
}
