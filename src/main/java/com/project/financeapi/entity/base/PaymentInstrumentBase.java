package com.project.financeapi.entity.base;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.project.financeapi.entity.Installment;
import com.project.financeapi.entity.Transaction;
import com.project.financeapi.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "payment_instruments")
@Getter
@Setter
@NoArgsConstructor
public abstract class PaymentInstrumentBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 25)
    private String name;

    @Column(nullable = false, name = "is_global")
    private Boolean isGlobal = false;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by")
    @Setter(AccessLevel.PROTECTED)
    private User createdBy;

    /**
     * Transações associadas a este instrumento (ex: compras, pagamentos, transferências).
     */
    @JsonManagedReference
    @OneToMany(mappedBy = "paymentInstrument", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Transaction> transactions = new ArrayList<>();


    public PaymentInstrumentBase(String name, User createdBy) {
        this.name = name;
        this.createdBy = createdBy;
    }


    /**
     * Adiciona uma transação a este instrumento.
     */
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        transaction.setPaymentInstrument(this);
    }

    /**
     * Remove uma transação deste instrumento.
     */
    public void removeTransaction(Transaction transaction) {
        transactions.remove(transaction);
        transaction.setPaymentInstrument(null);
    }

    /**
     * Retorna o saldo consolidado (créditos - débitos).
     */
    public BigDecimal getBalance() {
        return transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Retorna o total movimentado.
     */
    public BigDecimal getTotalTransactions() {
        return transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
