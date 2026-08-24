package com.project.financeapi.dto.Installments;

import com.project.financeapi.dto.transaction.TransactionResponseDTO;
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
        PaymentStatus status,
        LocalDate dueDate,
        LocalDate createdAt,
        List<TransactionResponseDTO> transactions

) {
}
