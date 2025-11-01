package com.project.financeapi.dto.payment;

import java.util.UUID;

public record PaymentMethodRequestDTO(
        String name,
        UUID paymentTypeId,
        PaymentMethodDetailsDTO details
) {
}
