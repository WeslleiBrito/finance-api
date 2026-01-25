package com.project.financeapi.dto.Installments;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


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

        @Future(message = "A data informada é inválida.")
        LocalDate dueDate,

        @NotNull(message = "O instrumento de pagamento deve ser informado.")
        UUID instrument
) {}
