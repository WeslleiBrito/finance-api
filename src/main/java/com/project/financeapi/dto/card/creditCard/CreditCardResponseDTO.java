package com.project.financeapi.dto.card.creditCard;

import com.project.financeapi.dto.bank.BankResponseDTO;
import com.project.financeapi.dto.card.cardBrand.CardBrandResponseDTO;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditCardResponseDTO(
        UUID id,
        String name,
        BigDecimal creditLimit,
        Integer closingDay,
        CardBrandResponseDTO cardBrand,
        BankResponseDTO bank,
        BigDecimal availableLimit,
        BigDecimal revolvingInterest,
        BigDecimal fine

) {
}
