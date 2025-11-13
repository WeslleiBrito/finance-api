package com.project.financeapi.dto.bank;

import com.project.financeapi.enums.BankStatus;

import java.util.UUID;

public record BankResponseDTO(
        UUID id,
        String name,
        String code,
        BankStatus bankStatus
) {
}
