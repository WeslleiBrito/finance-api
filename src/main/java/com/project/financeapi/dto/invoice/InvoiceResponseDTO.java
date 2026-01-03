package com.project.financeapi.dto.invoice;

import com.project.financeapi.dto.Installment.InstallmentResponseDTO;
import com.project.financeapi.dto.OperationType.OperationTypeResponseDTO;
import com.project.financeapi.dto.account.ResponseAccountDTO;
import com.project.financeapi.dto.user.UserResponseDTO;
import com.project.financeapi.enums.DocumentStatus;
import com.project.financeapi.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record InvoiceResponseDTO(
    UUID id,
    UUID accountId,
    UUID operationTypeId,
    LocalDate issueDate,
    PaymentStatus status,
    Integer quantityInstallments,
    BigDecimal totalAmount,
    BigDecimal totalPaid,
    BigDecimal totalDiscount,
    BigDecimal remainingBalance,
    List<InstallmentResponseDTO> installments
) {}
