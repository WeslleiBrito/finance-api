package com.project.financeapi.dto.transaction;

import com.project.financeapi.enumSystem.MovementDirection;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateManualAdjustmentTransactionDTO(

        @NotNull(message = "O valor do ajuste deve ser informado.")
        @Positive(message = "O valor do ajuste deve ser maior que zero.")
        BigDecimal amount,

        @NotNull(message = "A direção do ajuste deve ser informada.")
        MovementDirection direction,

        @NotNull(message = "A data do ajuste deve ser informada.")
        LocalDate paymentDate,

        @NotNull(message = "O motivo do ajuste deve ser informado.")
        String reason,

        @NotNull(message = "A conta deve ser informada.")
        UUID accountId,

        UUID paymentInstrumentId
) {}
