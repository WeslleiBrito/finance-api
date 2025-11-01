package com.project.financeapi.dto.card.cardBrand;

import com.project.financeapi.dto.user.UserResponseDTO;
import com.project.financeapi.enums.CardBrandStatus;

import java.time.LocalDate;

public record CardBrandResponseDTO(
        String id,
        String name,
        CardBrandStatus status,
        Boolean isGlobal,
        LocalDate createdAt
) {
}
