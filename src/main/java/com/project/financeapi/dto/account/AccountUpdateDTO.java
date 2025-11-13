package com.project.financeapi.dto.account;

import com.project.financeapi.enums.AccountStatus;
import com.project.financeapi.enums.AccountType;

import java.util.UUID;

public interface AccountUpdateDTO {
    String name();
    AccountType type();
    AccountStatus status();
    UUID bankId();
}
