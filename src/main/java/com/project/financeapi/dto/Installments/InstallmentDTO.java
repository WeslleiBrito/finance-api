package com.project.financeapi.dto.Installments;

import com.project.financeapi.enumSystem.MovementDirection;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.aspectj.bridge.IMessage;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InstallmentDTO (
        @NotNull(message = "O valor total da parcela precisa ser informado.")
        @Positive(message = "O valor da parcela precisa ser maior que zero.")
        BigDecimal amount,

        @NotNull(message = "O número da parcela deve ser informada.")
        @Positive(message = "O número da parcela precisa ser maior que zero.")
        Integer parcelNumber,

        @NotNull(message = "A data deve ser informada.")
        LocalDate dueDate,

        @NotNull(message = "A conta provisão deve ser informada na parcela.")
        UUID accountId,

        @NotNull(message = "O instrumento de pagamento deve ser informado.")
        UUID instrument,

        @NotNull(message = "Informe o sentido do recurso [OUTFLOW, INFLOW]")
        MovementDirection movementDirection
) {}
