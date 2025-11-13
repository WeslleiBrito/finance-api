package com.project.financeapi.dto.payment;

import com.project.financeapi.enums.InstrumentNature;
import com.project.financeapi.enums.PaymentType;

import java.time.LocalDateTime;
import java.util.UUID;

public interface PaymentMethodDetailsDTO {
    UUID id();
    PaymentType paymentType();
    Boolean isGlobal();
    LocalDateTime createdAt();
    InstrumentNature instrumentNature();
}
