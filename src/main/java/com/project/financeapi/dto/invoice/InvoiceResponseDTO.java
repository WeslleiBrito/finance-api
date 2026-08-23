package com.project.financeapi.dto.invoice;

import com.project.financeapi.dto.Installments.InstallmentResponseDTO;
import com.project.financeapi.dto.person.PersonResponseCompactDTO;
import com.project.financeapi.enumSystem.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record InvoiceResponseDTO(
    UUID id,
    UUID operationTypeId,
    PersonResponseCompactDTO person,
    LocalDate issueDate,
    PaymentStatus status,
    Integer quantityInstallments,
    BigDecimal totalAmount,
    BigDecimal totalPaid,
    BigDecimal totalDiscount,
    BigDecimal remainingBalance,
    List<InstallmentResponseDTO> installments
) {}
