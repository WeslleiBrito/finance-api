package com.project.financeapi.dto.bank;

import com.project.financeapi.enums.BankStatus;

public record BankUpdateRequestDTO(
        String name,
        String code,
        BankStatus bankStatus
) {
}
