package com.project.financeapi.dto.account.update;

import com.project.financeapi.dto.account.AccountUpdateDTO;
import com.project.financeapi.enumSystem.AccountStatus;
import com.project.financeapi.enumSystem.AccountType;

import java.util.UUID;

public record UpdatePaymentAccountRequestDTO(
        String name,
        AccountType type,
        AccountStatus status,
        UUID bankId,
        String provider
) implements AccountUpdateDTO {
}
