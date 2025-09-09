package com.project.financeapi.dto.phone;

import com.project.financeapi.enums.PhoneType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PhoneDTO(
        @NotBlank(message = "O número de telefone é obrigatório.")
        @Pattern(regexp = "^\\+?[0-9\\s()\\-]{8,20}$", message = "O número de telefone tem um formato inválido.")
        String number,

        @NotNull(message = "O tipo de telefone é obrigatório.")
        PhoneType type
) {}