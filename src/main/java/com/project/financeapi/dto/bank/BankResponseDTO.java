package com.project.financeapi.dto.bank;

import com.project.financeapi.dto.user.UserResponseDTO;
import com.project.financeapi.enums.BankStatus;

public record BankResponseDTO(
        String id,
        String name,
        String code,
        BankStatus bankStatus,
        UserResponseDTO user
) {
}
