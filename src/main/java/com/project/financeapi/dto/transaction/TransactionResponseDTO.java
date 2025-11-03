package com.project.financeapi.dto.transaction;

import com.project.financeapi.dto.Installment.InstallmentDTO;
import com.project.financeapi.dto.Installment.InstallmentResponseDTO;
import com.project.financeapi.dto.account.ResponseAccountDTO;
import com.project.financeapi.dto.user.UserResponseDTO;
import com.project.financeapi.enums.MovementType;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponseDTO(
        UUID id,
        UUID installmentId,
        UUID accountId,
        BigDecimal amount,
        MovementType movementType,
        Boolean isReversed,
        LocalDate paymentDate,
        LocalDateTime createdAt,
        String observations
) {}
