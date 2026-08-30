package com.project.financeapi.dto.account.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateAccountBase(

        @NotBlank(message = "O nome da conta é obrigatório")
        @Size(max = 30, message = "O nome da conta deve ter no máximo 30 caracteres")
        String name,

        @NotNull(message = "O valor inicial é obrigatório.")
        BigDecimal initialValue,

        UUID bankId
) {}
