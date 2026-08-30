package com.project.financeapi.entity;

import com.project.financeapi.dto.Installments.InstallmentDTO;
import com.project.financeapi.dto.payment.PaymentMethodDetailsDTO;
import com.project.financeapi.dto.payment.SimplePaymentMethodDetailsDTO;
import com.project.financeapi.entity.base.PaymentInstrumentBase;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.List;


@Entity
@Table(name = "simple_payment_instrument")
@PrimaryKeyJoinColumn(name = "id")
public class SimplePaymentInstrument extends PaymentInstrumentBase {

    @Override
    public List<InstallmentDTO> process(List<InstallmentDTO> installmentDTOS, LocalDate purchaseDate) {
        return installmentDTOS;
    }

    @Override
    public List<Installment> getInstallments() {
        return List.of(); // comportamento padrão
    }

    @Override
    public PaymentMethodDetailsDTO toDTO() {
        return new SimplePaymentMethodDetailsDTO(
                getId(),
                getPaymentType(),
                getCreatedAt(),
                getInstrumentNature()
        );
    }


}

