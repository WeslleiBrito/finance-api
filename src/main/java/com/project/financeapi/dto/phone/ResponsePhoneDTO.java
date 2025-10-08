package com.project.financeapi.dto.phone;

import com.project.financeapi.enums.PhoneType;

public record ResponsePhoneDTO(
        String id,
        String number,
        PhoneType phoneType
) {
}
