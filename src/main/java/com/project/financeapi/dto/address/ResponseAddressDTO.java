package com.project.financeapi.dto.address;

import java.util.UUID;

public record ResponseAddressDTO(
    UUID id,
    String street,
    String number,
    String neighborhood,
    String city,
    String state,
    String zipCode,
    String complement
) {
}
