package com.project.financeapi.dto.address;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AddressesListDTO(
        @NotNull(message = "A lista de endereços não pode ser nula.")
        @Size(min = 1, message = "Deve haver pelo menos um endereço na lista.")
        @Valid
        List<AddressDTO> addresses
) {}