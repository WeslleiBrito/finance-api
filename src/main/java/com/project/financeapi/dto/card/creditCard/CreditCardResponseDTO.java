package com.project.financeapi.dto.card.creditCard;

import com.project.financeapi.dto.bank.BankResponseDTO;
import com.project.financeapi.dto.card.cardBrand.CardBrandResponseDTO;

import java.math.BigDecimal;

public record CreditCardResponseDTO(
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
