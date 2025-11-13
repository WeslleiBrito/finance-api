package com.project.financeapi.dto.account;

import com.project.financeapi.enums.AccountStatus;
import com.project.financeapi.enums.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountResponseDTO {
    UUID id();
    String name();
    AccountType type();
    BigDecimal balance();
    AccountStatus status();
}
