package com.project.financeapi.dto.payment;


import com.project.financeapi.enums.InstrumentNature;
import com.project.financeapi.enums.PaymentType;

import java.time.LocalDateTime;
import java.util.UUID;

public record PixDetailsDTO(
        UUID id,
        PaymentType paymentType,
        Boolean isGlobal,
        LocalDateTime createdAt,
        InstrumentNature instrumentNature,
        UUID bankId,
        String keyType,  // CPF, Email, Telefone, Aleatória
        String keyValue
) implements PaymentMethodDetailsDTO {}
