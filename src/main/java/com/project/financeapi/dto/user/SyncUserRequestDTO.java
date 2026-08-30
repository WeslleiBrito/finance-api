package com.project.financeapi.dto.user;

import jakarta.validation.constraints.NotBlank;

public record SyncUserRequestDTO(
        @NotBlank(message = "O nome é obrigatório para criar a conta localmente.")
        String name
) {}