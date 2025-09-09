package com.project.financeapi.dto.user;

import com.project.financeapi.enums.UserStatus;

public record ResponseUserDTO(
        String id,
        String name,
        UserStatus userStatus
) {
}
