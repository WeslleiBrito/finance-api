package com.project.financeapi.dto.invoice;

import com.project.financeapi.dto.Installment.InstallmentResponseDTO;
import com.project.financeapi.dto.OperationType.OperationTypeResponseDTO;
import com.project.financeapi.dto.account.ResponseAccountDTO;
import com.project.financeapi.dto.user.ResponseUserDTO;
import com.project.financeapi.entity.OperationType;
import com.project.financeapi.enums.DocumentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InvoiceResponseDTO(
    String id,
    OperationTypeResponseDTO operationType,
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
