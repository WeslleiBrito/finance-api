package com.project.financeapi.entity;

import com.project.financeapi.dto.Installments.InstallmentDTO;
import com.project.financeapi.dto.payment.BankSlipDetailsDTO;
import com.project.financeapi.entity.base.PaymentInstrumentBase;
import com.project.financeapi.enumSystem.InstrumentNature;
import com.project.financeapi.enumSystem.PaymentType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

public class PaymentBankSlip extends PaymentInstrumentBase {

    @OneToMany(mappedBy = "paymentInstrument", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Installment> installments = new ArrayList<>();

    public PaymentBankSlip (){}

    public PaymentBankSlip (
            String name,
            User createBy
    )
    {
        super(name, createBy, InstrumentNature.PAYMENT, PaymentType.CASH);
    }

    @Override
    public BankSlipDetailsDTO toDTO() {

        return new BankSlipDetailsDTO(
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
