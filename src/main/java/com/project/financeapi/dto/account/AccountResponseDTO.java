package com.project.financeapi.dto.account;

import com.project.financeapi.dto.transaction.TransactionResponseDTO;
import com.project.financeapi.entity.Transaction;
import com.project.financeapi.enumSystem.AccountStatus;
import com.project.financeapi.enumSystem.AccountType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountResponseDTO {
    UUID id();
    String name();
    AccountType type();
    BigDecimal balance();
    AccountStatus status();
    List<TransactionResponseDTO> transactions();
}
