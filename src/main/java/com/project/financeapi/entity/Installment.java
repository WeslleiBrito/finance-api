package com.project.financeapi.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.project.financeapi.dto.Installments.InstallmentResponseDTO;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.entity.base.PaymentInstrumentBase;
import com.project.financeapi.enumSystem.MovementType;
import com.project.financeapi.enumSystem.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "installments")
@Getter
@Setter
public class Installment {

    @Id
    @Column(length = 36)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private AccountBase account;

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
            PaymentInstrumentBase paymentInstrument,
            AccountBase account
    ) {
        this.amount = amount;
        this.dueDate = dueDate;
        this.movementType = movementType;
        this.parcelNumber = parcelNumber;
        this.createdBy = createdBy;
        this.invoice = invoice;
        this.paymentInstrument = paymentInstrument;
        this.account = account;
    }

    /**
     * Retorna o total já amortizado nesta parcela (Apenas o Principal).
     */
    public BigDecimal getTotalPaid() {
        return transactions.stream()
                .filter(t -> t.getAmount() != null) // 🌟 Usando o getAmount() (Principal)
                .map(t -> {
                    BigDecimal value = t.getAmount();

                    return t.getMovementType() == MovementType.REVERSAL
                            ? value.negate()
                            : value;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Retorna o saldo em aberto da parcela.
     */
    public BigDecimal getRemainingBalance() {
        // 🌟 Como o getTotalPaid() agora é a amortização pura, basta subtrair!
        return this.amount.subtract(getTotalPaid());
    }

    /**
     * Verifica se a parcela está quitada.
     */
    public PaymentStatus isPaid() {
        // 🌟 Limpamos a gambiarra do desconto duplo
        if (getRemainingBalance().compareTo(BigDecimal.ZERO) <= 0) {
            return PaymentStatus.FINALIZED;
        } else if (getRemainingBalance().compareTo(this.amount) == 0) {
            return PaymentStatus.OPEN;
        }
        return PaymentStatus.PARTIALLY_PAID;
    }


    public BigDecimal getTotalDiscount() {
        return transactions.stream()
                .filter(t -> t.getEffectiveAmount() != null) // Filtra transações inválidas
                .map(t -> {
                    // 1. Tratamento de Null: Se for null, considera ZERO
                    BigDecimal discountVal = t.getDiscount() == null ? BigDecimal.ZERO : t.getDiscount();

                    // 2. Tratamento de Estorno: Se a transação for estorno, o desconto deve ser anulado (negativado)
                    return t.getMovementType() == MovementType.REVERSAL
                            ? discountVal.negate()
                            : discountVal;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalInterest() {
        return transactions.stream()
                .filter(t -> t.getInterest() != null) // Filtra transações inválidas
                .map(t -> {
                    // 1. Tratamento de Null: Se for null, considera ZERO
                    BigDecimal interestVal = t.getInterest();

                    // 2. Tratamento de Estorno: Se a transação for estorno, o desconto deve ser anulado (negativado)
                    return t.getMovementType() == MovementType.REVERSAL
                            ? interestVal.negate()
                            : interestVal;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalFine() {
        return transactions.stream()
                .filter(t -> t.getFine() != null) // Filtra transações inválidas
                .map(t -> {
                    // 1. Tratamento de Null: Se for null, considera ZERO
                    BigDecimal fineVal = t.getFine();

                    // 2. Tratamento de Estorno: Se a transação for estorno, o desconto deve ser anulado (negativado)
                    return t.getMovementType() == MovementType.REVERSAL
                            ? fineVal.negate()
                            : fineVal;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }



    public InstallmentResponseDTO toResponse(){
        return new InstallmentResponseDTO(
                this.getId(),
                this.getInvoice().getId(),
                this.getAccount().getId(),
                this.getPaymentInstrument().getId(),
                this.getParcelNumber(),
                this.getAmount(),
                this.getTotalPaid(),
                this.getTotalInterest(),
                this.getTotalFine(),
                this.getTotalDiscount(),
                this.movementType,
                this.isPaid(),
                this.getDueDate(),
                this.getCreatedAt(),
                this.getTransactions().stream().map(
                        Transaction::toResponse
                ).toList()
        );
    }

}
