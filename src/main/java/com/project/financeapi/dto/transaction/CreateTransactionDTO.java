package com.project.financeapi.dto.transaction;

import com.project.financeapi.enumSystem.MovementDirection;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTransactionDTO(

        @NotNull(message = "O valor da transação deve ser informado.")
        @Positive(message = "O valor da transação deve ser maior que zero.")
        BigDecimal amount,

        @NotNull(message = "A data do pagamento deve ser informada.")
        LocalDate paymentDate,

        String observations,

        @NotNull(message = "A parcela deve ser informada.")
        UUID installmentId,

        @NotNull(message = "A conta deve ser informada.")
        UUID accountId,

        UUID paymentInstrumentId,

        @PositiveOrZero(message = "O valor do juros deve ser maior ou igual a zero.")
        BigDecimal interest,

        @PositiveOrZero(message = "O valor da multa deve ser maior ou igual a zero.")
        BigDecimal fine,

        @PositiveOrZero(message = "O valor do desconto deve ser maior ou igual a zero.")
        BigDecimal discount

) {}
