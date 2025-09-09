package com.project.financeapi.dto.Installment;

import com.project.financeapi.dto.transaction.TransactionResponseDTO;
import com.project.financeapi.dto.user.ResponseUserDTO;
import com.project.financeapi.enums.MovementType;
import com.project.financeapi.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InstallmentResponseDTO(
        String id,
        BigDecimal amount,
        LocalDate createdAt,
        LocalDate dueDate,
        MovementType movementType,
        PaymentStatus status,
        Integer parcelNumber,
        ResponseUserDTO responseUser,
        String document_id,
        List<TransactionResponseDTO> transactionResponse

) {
}
