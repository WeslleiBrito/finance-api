package com.project.financeapi.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.project.financeapi.dto.OperationType.OperationTypeResponseDTO;
import com.project.financeapi.dto.invoice.InvoiceResponseDTO;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.entity.base.PersonBase;
import com.project.financeapi.enums.DocumentStatus;
import com.project.financeapi.enums.MovementType;
import com.project.financeapi.enums.PaymentStatus;
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


    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate issueDate = LocalDate.now();

    public int getQuantityInstallments() {
        return this.installments.size();
    }


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status = DocumentStatus.OPEN;


    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // Quem emitiu / recebeu este documento

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    @JsonBackReference
    private PersonBase person; // pode ser cliente ou fornecedor

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountBase account;


    // Parcelas deste documento
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Installment> installments = new ArrayList<>();

    public Invoice() {}

    public Invoice(
            BigDecimal totalAmount,
            User createdBy,
            PersonBase person,
            AccountBase account,
            OperationType operationType
    ) {
        this.totalAmount = totalAmount;
        this.createdBy = createdBy;
        this.person = person;
        this.account = account;
        this.operationType = operationType;
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
     * Retorna o saldo restante.
     */
    public BigDecimal getRemainingBalance() {
        return totalAmount.subtract(getTotalPaid().add(getTotalDiscount()));
    }

    public InvoiceResponseDTO toResponse() {
        return new InvoiceResponseDTO(
                this.getId(),
                this.getAccount().getId(),
                this.getOperationType().getId(),
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

