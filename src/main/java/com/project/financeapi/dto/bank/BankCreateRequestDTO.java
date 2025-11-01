package com.project.financeapi.dto.bank;

import jakarta.validation.constraints.NotNull;

public record BankCreateRequestDTO(
        @NotNull(message = "O nome do banco é obrigatório")
        String name,
        String code
) {
}
