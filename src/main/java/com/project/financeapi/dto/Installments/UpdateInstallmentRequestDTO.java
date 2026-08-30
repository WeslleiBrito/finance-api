package com.project.financeapi.dto.Installments;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateInstallmentRequestDTO(
        @NotNull(message = "O valor da parcela deve ser informado.")
        @Positive(message = "O valor deve ser maior que zero.")
        BigDecimal amount,

        @NotNull(message = "A data de vencimento deve ser informada.")
        LocalDate dueDate,

        @NotNull(message = "A conta provisão deve ser informada.")
        UUID accountId,

        UUID paymentInstrumentId
) {}