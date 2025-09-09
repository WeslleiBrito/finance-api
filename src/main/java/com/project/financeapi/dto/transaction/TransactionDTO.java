package com.project.financeapi.dto.transaction;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionDTO(

        @NotNull(message = "A parcela deve ser informada.")
        String installmentId,

        @NotNull(message = "O valor da transação deve ser informado.")
        @Positive(message = "O valor da transação deve ser maior que zero.")
        BigDecimal amount,

        LocalDate paymentDate,

        String observations
) {}
