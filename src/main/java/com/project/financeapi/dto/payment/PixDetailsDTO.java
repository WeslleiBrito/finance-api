package com.project.financeapi.dto.payment;


import java.util.UUID;

public record PixDetailsDTO(
        UUID bankId,
        String keyType,  // CPF, Email, Telefone, Aleatória
        String keyValue
) implements PaymentMethodDetailsDTO {}
