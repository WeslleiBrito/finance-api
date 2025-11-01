package com.project.financeapi.dto.payment;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditCardDetailsDTO(
        UUID bankId,
        String cardNumber,
        String cardHolderName,
        BigDecimal creditLimit,
        String dueDay, // dia de fechamento
        String expirationDate // opcional
) implements PaymentMethodDetailsDTO {
}
