package com.project.financeapi.dto.user;

import com.project.financeapi.enums.UserStatus;

public record UserResponseDTO(
        String id,
        String name,
        UserStatus userStatus
) {
}
