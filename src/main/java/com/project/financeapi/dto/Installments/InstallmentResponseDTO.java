package com.project.financeapi.dto.Installments;

import com.project.financeapi.dto.transaction.TransactionResponseDTO;
import com.project.financeapi.enumSystem.MovementDirection;
import com.project.financeapi.enumSystem.MovementType;
import com.project.financeapi.enumSystem.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record InstallmentResponseDTO(
        UUID id,
        UUID invoiceId,
        UUID accountId,
        UUID paymentInstrumentId,
        Integer parcelNumber,
        BigDecimal amount,
        BigDecimal totalPaid,
        BigDecimal totalInterest,
        BigDecimal totalFine,
        BigDecimal TotalDiscount,
        MovementType movementType,
        MovementDirection movementDirection,
        PaymentStatus status,
        LocalDate dueDate,
        LocalDate createdAt,
        String personName,
        Integer quantityInstallments,
        String operationName,
        List<TransactionResponseDTO> transactions

) {
}
