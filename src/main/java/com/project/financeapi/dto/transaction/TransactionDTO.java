package com.project.financeapi.dto.transaction;

import com.project.financeapi.enums.MovementType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionDTO(

        @NotNull(message = "A parcela deve ser informada.")
        UUID installmentId,

        @NotNull(message = "O id da conta de operação é obrigatório.")
        UUID accountId,

        @NotNull(message = "O id do instrumento de pagamento deve ser informado.")
        UUID paymentInstrumentId,

        @NotNull(message = "O tipo de movimento deve ser informado.")
        MovementType movementType,

        @NotNull(message = "O valor da transação deve ser informado.")

        @DecimalMin(value = "0.01", message = "O valor mínimo de uma transação é 0,01 centavos.")
        BigDecimal amount,

        LocalDate paymentDate,
        Boolean isReversed,
        String observations
) {}
