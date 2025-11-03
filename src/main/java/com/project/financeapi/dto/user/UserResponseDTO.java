package com.project.financeapi.dto.user;

import com.project.financeapi.enums.UserStatus;

import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String name,
        UserStatus userStatus
) {
}
