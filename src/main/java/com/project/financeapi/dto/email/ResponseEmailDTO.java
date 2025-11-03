package com.project.financeapi.dto.email;

import java.util.UUID;

public record ResponseEmailDTO(
        UUID id,
        String email
) {
}
