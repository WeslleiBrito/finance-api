package com.project.financeapi.dto.account;

import com.project.financeapi.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateAccountRequestDTO(

        @NotBlank(message = "O nome da conta é obrigatório")
        @Size(max = 30, message = "O nome da conta deve ter no máximo 30 caracteres")
        String name,

        @NotNull(message = "O tipo de conta é obrigatório")
        AccountType type,

        @Positive(message = "O valor inicial da conta precisa ser maior que zero.")
        BigDecimal initialValue
) {}
