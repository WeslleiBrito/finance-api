package com.project.financeapi.dto.card.cardBrand;

import com.project.financeapi.enums.CardBrandStatus;

import java.time.LocalDate;
import java.util.UUID;

public record CardBrandResponseDTO(
        UUID id,
        String name,
        CardBrandStatus status,
        Boolean isGlobal,
        LocalDate createdAt
) {
}
