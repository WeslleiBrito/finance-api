package com.project.financeapi.entity;

import com.project.financeapi.dto.Installments.InstallmentDTO;
import com.project.financeapi.dto.payment.PIXDetailsDTO;
import com.project.financeapi.entity.base.PaymentInstrumentBase;
import com.project.financeapi.enumSystem.InstrumentNature;
import com.project.financeapi.enumSystem.PaymentType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

public class PaymentPIX extends PaymentInstrumentBase {

    @OneToMany(mappedBy = "paymentInstrument", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Installment> installments = new ArrayList<>();

    public PaymentPIX (){}

    public PaymentPIX(
            String name,
            User createBy
    )
    {
        super(name, createBy, InstrumentNature.PAYMENT, PaymentType.CASH);
    }

    @Override
    public PIXDetailsDTO toDTO() {
        return new PIXDetailsDTO(
                this.getId(),
                this.getPaymentType(),
                this.getCreatedAt(),
                this.getInstrumentNature()
        );
    }

    @Override
    public List<InstallmentDTO> process(List<InstallmentDTO> installments) {
        return installments;
    }

    @Override
    public List<Installment> getInstallments() {
        return installments;
    }
}
