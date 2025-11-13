package com.project.financeapi.dto.account.create;

import jakarta.validation.constraints.NotNull;

public record CreatePaymentAccountRequestDTO(
        @NotNull(message = "O baseAccount deve ser informado.")
        CreateAccountBase baseAccount,
        String provider
) {
}
