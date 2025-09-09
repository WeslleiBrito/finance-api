package com.project.financeapi.dto.phone;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PhoneCreateRequestDTO(
        @NotNull(message = "O id da pessoa é obrigatório.")
        @NotBlank(message = "O id da pessoa não pode ser vazio.")
        String idPerson,

        @Size(min = 1, message = "A lista de e-mail não pode ser vazia.")
        PhoneListDTO phoneList
) {
}
