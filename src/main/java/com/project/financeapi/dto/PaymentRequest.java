package com.project.financeapi.dto;

import java.math.BigDecimal;

public record PaymentRequest(
        String accountId,
        BigDecimal amount,
        String description
) {
}
