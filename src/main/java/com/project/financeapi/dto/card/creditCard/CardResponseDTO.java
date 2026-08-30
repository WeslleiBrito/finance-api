package com.project.financeapi.dto.card.creditCard;

import com.project.financeapi.dto.bank.*;
import com.project.financeapi.dto.card.cardBrand.CardBrandResponseDTO;
import com.project.financeapi.enumSystem.CardStatus;

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
