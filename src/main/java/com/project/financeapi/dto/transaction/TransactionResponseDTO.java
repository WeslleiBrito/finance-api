package com.project.financeapi.dto.transaction;

import com.project.financeapi.enums.MovementDirection;
import com.project.financeapi.enums.MovementType;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponseDTO(
        UUID id,
        UUID installmentId,
        UUID accountId,
        UUID paymentInstrumentId,

        BigDecimal amount,
        BigDecimal interest,
        BigDecimal fine,
        BigDecimal discount,
        BigDecimal effectiveAmount,

        MovementType movementType,
        MovementDirection movementDirection,

        UUID reversedTransactionId,
        boolean reversed,

        LocalDate paymentDate,
        LocalDateTime createdAt,
        String observations
) {}
