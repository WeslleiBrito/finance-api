package com.project.financeapi.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.project.financeapi.entity.base.PaymentInstrumentBase;
import com.project.financeapi.enums.MovementType;
import com.project.financeapi.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "installments")
@Getter
@Setter
public class Installment {

    @Id
    @Column(length = 36)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, name = "due_date")
    private LocalDate dueDate  = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;

    @Column(nullable = false, name = "created_at")
    private LocalDate createdAt  = LocalDate.now();

    @Column(name = "parcel_number", nullable = false)
    private Integer parcelNumber;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_instrument")
    private PaymentInstrumentBase paymentInstrument;


    // Transações que foram feitas para quitar essa parcela
    @JsonBackReference
    @OneToMany(mappedBy = "installment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    public Installment() {}

    public Installment(
            BigDecimal amount,
            LocalDate dueDate,
            MovementType movementType,
            Integer parcelNumber,
            User createdBy,
            Invoice invoice,
            PaymentInstrumentBase paymentInstrument
    )
    {
        this.amount = amount;
        this.dueDate = dueDate;
        this.movementType = movementType;
        this.parcelNumber = parcelNumber;
        this.createdBy = createdBy;
        this.invoice = invoice;
        this.paymentInstrument = paymentInstrument;
    }


    /**
     * Helper para manter consistência bidirecional
     */
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        transaction.setInstallment(this);
    }

    public void removeTransaction(Transaction transaction) {
        transactions.remove(transaction);
        transaction.setInstallment(null);
    }

    /**
     * Retorna o total já pago/recebido nesta parcela.
     */
    public BigDecimal getTotalPaid() {
        return transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Retorna o saldo em aberto da parcela.
     */
    public BigDecimal getRemainingBalance() {
        return this.amount.subtract(getTotalPaid());
    }

    /**
     * Verifica se a parcela está quitada.
     */
    public boolean isPaid() {
        return getRemainingBalance().compareTo(BigDecimal.ZERO) <= 0;
    }


}
