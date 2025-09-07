package com.project.financeapi.dto.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record AddressDTO(
        @NotBlank(message = "A rua é obrigatória.")
        @Size(max = 150, message = "A rua não pode ter mais de 150 caracteres.")
        String street,

        @NotBlank(message = "O número é obrigatório.")
        @Size(max = 10, message = "O número não pode ter mais de 10 caracteres.")
        String number,

        @Size(max = 150, message = "O complemento não pode ter mais de 150 caracteres.")
        String complement,

        @NotBlank(message = "A cidade é obrigatória.")
        @Size(max = 100, message = "A cidade não pode ter mais de 100 caracteres.")
        String city,

        @NotBlank(message = "O estado é obrigatório.")
        @Size(min = 2, max = 20, message = "O estado deve ter 2 a 20 caracteres.")
        String state,

        @NotBlank(message = "O CEP é obrigatório.")
        @Pattern(regexp = "\\d{5}-\\d{3}", message = "O CEP deve estar no formato 99999-999.")
        String zipCode
) {}