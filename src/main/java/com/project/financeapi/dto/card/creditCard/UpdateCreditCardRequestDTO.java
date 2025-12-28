package com.project.financeapi.dto.card.creditCard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateCreditCardRequestDTO(
        String name,
        BigDecimal creditLimit,
        Integer closingDay,
        Integer dueDay,
        UUID cardBrandId,
        UUID bankId,
        BigDecimal revolvingInterest,
        BigDecimal fine,
        LocalDate expirationDate
) {
}
