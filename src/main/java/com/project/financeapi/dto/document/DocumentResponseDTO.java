package com.project.financeapi.dto.document;

import com.project.financeapi.dto.Installment.InstallmentResponseDTO;
import com.project.financeapi.dto.account.ResponseAccountDTO;
import com.project.financeapi.dto.user.ResponseUserDTO;
import com.project.financeapi.enums.DocumentStatus;
import com.project.financeapi.enums.MovementType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DocumentResponseDTO(
    String id,
    LocalDate issueDate,
    DocumentStatus status,
    Integer quantityInstallments,
    BigDecimal totalAmount,
    BigDecimal totalPaid,
    BigDecimal remainingBalance,
    ResponseUserDTO createdBy,
    ResponseAccountDTO account,
    List<InstallmentResponseDTO> installments
) {}
