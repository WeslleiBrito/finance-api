package com.project.financeapi.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.entity.base.PersonBase;
import com.project.financeapi.enums.DocumentStatus;
import com.project.financeapi.enums.MovementType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoice")
@Getter
@Setter
public class Invoice {

    @Id
    @Column(length = 36)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operation_type_id", nullable = false)
    private OperationType operationType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate issueDate = LocalDate.now();

    public int getQuantityInstallments() {
        return this.getInstallments().size();
    }

    /**
     * Retorna o total já pago em todas as parcelas.
     */
    public BigDecimal getTotalPaid() {
        return installments.stream()
                .map(Installment::getTotalPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Retorna o saldo em aberto.
     */
    public BigDecimal getRemainingBalance() {
        return totalAmount.subtract(getTotalPaid());
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

    public Invoice(BigDecimal totalAmount,
                   User createdBy, PersonBase person, AccountBase account
    ) {
        this.totalAmount = totalAmount;
        this.createdBy = createdBy;
        this.person = person;
        this.account = account;
    }

    /**
     * Atualiza o status do documento com base nas parcelas.
     */
    public void updateStatus() {
        boolean allPaid = installments.stream().allMatch(Installment::isPaid);
        boolean anyPaid = installments.stream().anyMatch(i -> i.getTotalPaid().compareTo(BigDecimal.ZERO) > 0);

        if (allPaid) {
            this.status = DocumentStatus.FINALIZED;
        } else if (anyPaid) {
            this.status = DocumentStatus.PARTIALLY_PAID;
        } else {
            this.status = DocumentStatus.OPEN;
        }
    }

    /**
     * Adiciona uma parcela ao documento.
     */
    public void addInstallment(Installment installment) {
        installment.setInvoice(this);
        this.installments.add(installment);
    }

    /**
     * Valida se a soma das parcelas confere com o valor total do documento.
     */
    public boolean validateInstallmentsTotal() {
        BigDecimal total = installments.stream()
                .map(Installment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.compareTo(this.totalAmount) == 0;
    }


}

