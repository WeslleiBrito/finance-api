package com.project.financeapi.dto.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddressCreateRequestDTO(
        @NotNull(message = "O id da pessoa é obrigatório.")
        @NotBlank(message = "O id da pessoa não pode ser vazio.")
        String idPerson,

        @Size(min = 1, message = "A lista de endereços não pode ser vazia.")
        AddressesListDTO addressesList
) {
}
