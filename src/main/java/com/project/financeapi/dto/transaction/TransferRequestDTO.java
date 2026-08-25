package com.project.financeapi.dto.transaction;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransferRequestDTO(
        @NotNull(message = "A conta de origem é obrigatória.")
        UUID sourceAccountId,

        @NotNull(message = "A conta de destino é obrigatória.")
        UUID destinationAccountId,

        @NotNull(message = "O valor é obrigatório.")
        @Positive(message = "O valor da transferência deve ser maior que zero.")
        BigDecimal amount,

        @NotNull(message = "A data da transferência é obrigatória.")
        LocalDate transferDate,

        String observations
) {}