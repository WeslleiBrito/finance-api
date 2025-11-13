package com.project.financeapi.dto.account.update;

import com.project.financeapi.dto.account.AccountUpdateDTO;
import com.project.financeapi.enums.AccountStatus;
import com.project.financeapi.enums.AccountType;

import java.util.UUID;

public record UpdatePaymentAccountRequestDTO(
        String name,
        AccountType type,
        AccountStatus status,
        UUID bankId,
        String provider
) implements AccountUpdateDTO {
}
