package com.project.financeapi.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.project.financeapi.dto.invoice.InvoiceResponseDTO;
import com.project.financeapi.dto.person.PersonResponseCompactDTO;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.entity.base.PersonBase;
import com.project.financeapi.enumSystem.DocumentStatus;
import com.project.financeapi.enumSystem.MovementDirection;
import com.project.financeapi.enumSystem.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "invoice")
@Getter
@Setter
public class Invoice {

    @Id
    @Column(length = 36)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operation_type_id", nullable = false)
    private OperationType operationType;

    @Column(nullable = false)
    private LocalDate issueDate = LocalDate.now();

    public int getQuantityInstallments() {
        return this.installments.size();
    }


    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // Quem emitiu / recebeu este documento

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    @JsonBackReference
    private PersonBase person; // pode ser cliente ou fornecedor

    // Parcelas deste documento
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Installment> installments = new ArrayList<>();

    public Invoice() {}

    public Invoice(
            User createdBy,
            PersonBase person,
            OperationType operationType
    ) {
        this.createdBy = createdBy;
        this.person = person;
        this.operationType = operationType;
    }

    /**
     * O Total da Fatura é a soma nominal de todas as suas parcelas.
     */
    public BigDecimal getTotalAmount() {
        if (installments == null || installments.isEmpty()) return BigDecimal.ZERO;

        return installments.stream()
                .map(Installment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Retorna o status geral da fatura com base nas parcelas.
     */
    public PaymentStatus getPaymentStatus() {
        boolean allPaid = installments.stream()
                .allMatch(i -> i.isPaid() == PaymentStatus.FINALIZED);

        boolean allOpen = installments.stream()
                .allMatch(i -> i.isPaid() == PaymentStatus.OPEN);

        if (allPaid) return PaymentStatus.FINALIZED;
        if (allOpen) return PaymentStatus.OPEN;

        return PaymentStatus.PARTIALLY_PAID;
    }

    /**
     * Retorna o total já pago em todas as parcelas.
     */
    public BigDecimal getTotalPaid() {
        return installments.stream()
                .map(Installment::getTotalPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalDiscount() {
        return installments.stream().map(Installment::getTotalDiscount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Retorna o saldo restante da fatura inteira.
     */
    public BigDecimal getRemainingBalance() {
        // 🌟 O getTotalPaid() das parcelas já reflete a amortização total.
        // Removido o ".add(getTotalDiscount())" que duplicava a dedução.
        return getTotalAmount().subtract(getTotalPaid());
    }
    public InvoiceResponseDTO toResponse() {
        return new InvoiceResponseDTO(
                this.getId(),
                this.getOperationType().getId(),
                new PersonResponseCompactDTO(
                        this.person.getId(),
                        this.person.getName()
                ),
                this.getIssueDate(),
                this.getPaymentStatus(),
                this.getQuantityInstallments(),
                this.getTotalAmount(),
                this.getTotalPaid(),
                this.getTotalDiscount(),
                this.getRemainingBalance(),
                getInstallments().stream().map(
                        Installment::toResponse
                ).toList()
        );
    }

}

