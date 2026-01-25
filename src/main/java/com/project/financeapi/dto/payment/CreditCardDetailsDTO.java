package com.project.financeapi.dto.payment;

import com.project.financeapi.dto.bank.*;
import com.project.financeapi.dto.card.cardBrand.CardBrandResponseDTO;
import com.project.financeapi.enumSystem.InstrumentNature;
import com.project.financeapi.enumSystem.InstrumentStatus;
import com.project.financeapi.enumSystem.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreditCardDetailsDTO(
        UUID id,
        PaymentType paymentType,
        LocalDateTime createdAt,
        InstrumentNature instrumentNature,
        LocalDate expirationDate, // opcional
        String cardHolderName,
        Integer closeDay,
        Integer dueDay, // dia de vencimento
        BigDecimal creditLimit,
        BigDecimal availableLimit,
        BigDecimal RevolvingInterest,
        BigDecimal fine,
        InstrumentStatus status,
        CardBrandResponseDTO cardBrand,
        BankResponseDTO bank
) implements PaymentMethodDetailsDTO {
}
