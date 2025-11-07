package com.project.financeapi.dto.account.create;

import jakarta.validation.constraints.NotNull;

public record CreateWalletAccountRequestDTO(
        @NotNull(message = "O baseAccount deve ser informado.")
        CreateAccountBase baseAccount
) {
}
