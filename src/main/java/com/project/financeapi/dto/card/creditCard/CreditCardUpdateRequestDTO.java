package com.project.financeapi.dto.card.creditCard;

import com.project.financeapi.entity.Bank;
import com.project.financeapi.entity.CardBrand;

import java.math.BigDecimal;

public record CreditCardUpdateRequestDTO (
        String name,
        BigDecimal creditLimit,
        Integer closingDay,
        Integer dueDay,
        CardBrand cardBrand,
        Bank bank,
        BigDecimal revolvingInterest,
        BigDecimal fine
)
{
}
