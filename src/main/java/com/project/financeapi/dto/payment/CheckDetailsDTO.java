package com.project.financeapi.dto.payment;

import java.util.UUID;

public record CheckDetailsDTO(
        UUID bankId,
        String checkNumber,
        String accountNumber,
        String agencyNumber,
        String issuerName
) implements PaymentMethodDetailsDTO {}
