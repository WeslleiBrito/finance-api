package com.project.financeapi.dto.person;

import java.util.UUID;

public record PersonResponseCompactDTO(
        UUID id,
        String name
) {
}
