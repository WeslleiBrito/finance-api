package com.project.financeapi.dto.bank;

import java.util.UUID;

public record BankResponseDTO(
        UUID id,
        String name,
        String code
) {
}
