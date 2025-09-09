package com.project.financeapi.dto.transaction;

import com.project.financeapi.dto.user.ResponseUserDTO;
import com.project.financeapi.enums.MovementType;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionResponseDTO(
        String id,
        String installmentId,
        String documentId,
        String accountId,
        MovementType movementType,
        BigDecimal amount,
        LocalDate issueDate,
        LocalDate dueDate,
        LocalDate paymentDate,
        ResponseUserDTO createdBy,
        String observations,
        LocalDateTime createdAt
) {}
