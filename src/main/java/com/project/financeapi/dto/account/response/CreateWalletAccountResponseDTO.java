package com.project.financeapi.dto.account.response;

import com.project.financeapi.dto.account.AccountResponseDTO;
import com.project.financeapi.dto.transaction.TransactionResponseDTO;
import com.project.financeapi.enumSystem.AccountStatus;
import com.project.financeapi.enumSystem.AccountType;


import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateWalletAccountResponseDTO(
        UUID id,
        String name,
        AccountType type,
        BigDecimal balance,
        BigDecimal initialValue,
        AccountStatus status,
        List<TransactionResponseDTO> transactions
) implements AccountResponseDTO {
}
