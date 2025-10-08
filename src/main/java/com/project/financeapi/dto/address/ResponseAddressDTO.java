package com.project.financeapi.dto.address;

public record ResponseAddressDTO(
    String id,
    String street,
    String number,
    String neighborhood,
    String city,
    String state,
    String zipCode,
    String complement
) {
}
