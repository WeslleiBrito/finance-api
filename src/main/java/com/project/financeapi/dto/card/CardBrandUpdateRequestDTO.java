package com.project.financeapi.dto.card;

import com.project.financeapi.enums.CardBrandStatus;

public record CardBrandUpdateRequestDTO(
        String name,
        CardBrandStatus cardBrandStatus
) {
}
