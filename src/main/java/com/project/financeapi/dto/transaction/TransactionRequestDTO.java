package com.project.financeapi.dto.transaction;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record TransactionRequestDTO(
        @NotNull(message = "O id do usuário deve ser informado.")
        UUID userId,
        List<TransactionDTO> itens

) {}
