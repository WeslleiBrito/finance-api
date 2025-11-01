package com.project.financeapi.dto.card;

import jakarta.validation.constraints.NotNull;

public record CardBrandCreateRequestDTO(
        @NotNull(message = "O nome da bandeira não foi informado")
        String name
) {
}
