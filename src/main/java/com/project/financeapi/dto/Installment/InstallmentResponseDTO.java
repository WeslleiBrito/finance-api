package com.project.financeapi.dto.Installment;

import com.project.financeapi.dto.transaction.TransactionResponseDTO;
import com.project.financeapi.dto.user.UserResponseDTO;
import com.project.financeapi.enums.MovementType;
import com.project.financeapi.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record InstallmentResponseDTO(
        UUID id,
        BigDecimal amount,
        LocalDate createdAt,
        LocalDate dueDate,
        MovementType movementType,
        PaymentStatus status,
        Integer parcelNumber,
        UUID invoiceId,
        List<TransactionResponseDTO> transactions

) {
}
