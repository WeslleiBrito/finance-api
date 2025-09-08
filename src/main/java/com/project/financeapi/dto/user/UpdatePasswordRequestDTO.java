package com.project.financeapi.dto.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdatePasswordRequestDTO(
        @NotNull(message = "A nova senha não foi informada.")
        @Size(min = 4, message = "A senha deve ter no mínimo 4 caracteres.")
        String password

) {
}
