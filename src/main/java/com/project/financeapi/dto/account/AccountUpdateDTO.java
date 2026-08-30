package com.project.financeapi.dto.account;

import com.project.financeapi.enumSystem.AccountType;

import java.util.UUID;

public interface AccountUpdateDTO {
    String name();
    AccountType type();
    UUID bankId();
}
