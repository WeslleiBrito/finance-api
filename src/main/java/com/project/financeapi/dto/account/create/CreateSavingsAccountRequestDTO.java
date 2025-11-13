package com.project.financeapi.dto.account.create;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateSavingsAccountRequestDTO(
        @NotNull(message = "O baseAccount deve ser informado.")
        CreateAccountBase baseAccount,
        BigDecimal interestRate
) {
}
