package com.project.financeapi.dto.card.creditCard;

import com.project.financeapi.dto.bank.BankResponseDTO;
import com.project.financeapi.dto.card.cardBrand.CardBrandResponseDTO;
import com.project.financeapi.enums.CardStatus;

import java.time.LocalDate;
import java.util.UUID;


public interface CardResponseDTO {
    UUID id();
    String name();
    LocalDate expirationDate();
    CardBrandResponseDTO cardBrand();
    CardStatus status();
    BankResponseDTO bank();
}
