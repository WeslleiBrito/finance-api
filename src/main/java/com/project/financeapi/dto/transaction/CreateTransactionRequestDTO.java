package com.project.financeapi.dto.transaction;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateTransactionRequestDTO(
        @NotEmpty(message = "A lista de transações vazia.")
        List<CreateTransactionDTO> transactions
) {
}
