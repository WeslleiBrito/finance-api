package com.project.financeapi.dto.card.cardBrand;

import com.project.financeapi.enums.CardBrandStatus;

public record CardBrandUpdateRequestDTO(
        String name,
        CardBrandStatus cardBrandStatus
) {
}
