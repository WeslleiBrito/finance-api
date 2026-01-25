package com.project.financeapi.dto.account.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateAccountBase(

        @NotBlank(message = "O nome da conta é obrigatório")
        @Size(max = 30, message = "O nome da conta deve ter no máximo 30 caracteres")
        String name,

        @Positive(message = "O valor inicial da conta precisa ser maior que zero.")
        BigDecimal initialValue,

        UUID bankId
) {}
