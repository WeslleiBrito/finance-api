package com.project.financeapi.dto.payment;

import com.project.financeapi.enumSystem.InstrumentNature;
import com.project.financeapi.enumSystem.PaymentType;

import java.time.LocalDateTime;
import java.util.UUID;

public interface PaymentMethodDetailsDTO {
    UUID id();
    PaymentType paymentType();
    LocalDateTime createdAt();
    InstrumentNature instrumentNature();
}
