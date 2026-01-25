package com.project.financeapi.dto.account;

import com.project.financeapi.enumSystem.AccountStatus;
import com.project.financeapi.enumSystem.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountResponseDTO {
    UUID id();
    String name();
    AccountType type();
    BigDecimal balance();
    AccountStatus status();
}
