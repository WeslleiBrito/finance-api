package com.project.financeapi.dto.document;

import com.project.financeapi.dto.Installment.InstallmentResponseDTO;
import com.project.financeapi.dto.user.ResponseUserDTO;
import com.project.financeapi.enums.MovementType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DocumentResponseDTO(
    String id,
    String description,
    BigDecimal totalAmount,
    MovementType movementType,
    LocalDate issueDate,
    Integer quantityInstallments,
    BigDecimal totalPaid,
    BigDecimal remainingBalance,
    ResponseUserDTO createdBy,
    List<InstallmentResponseDTO> installments
) {}
