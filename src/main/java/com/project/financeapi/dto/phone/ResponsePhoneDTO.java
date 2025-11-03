package com.project.financeapi.dto.phone;

import com.project.financeapi.enums.PhoneType;

import java.util.UUID;

public record ResponsePhoneDTO(
        UUID id,
        String number,
        PhoneType phoneType
) {
}
