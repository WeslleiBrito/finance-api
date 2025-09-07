package com.project.financeapi.entity;

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
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    // Transações que foram feitas para quitar essa parcela
    @OneToMany(mappedBy = "installment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    public Installment() {}

    public Installment(BigDecimal amount, LocalDate dueDate, Document document) {
        this.amount = amount;
        this.dueDate = dueDate != null ? dueDate : LocalDate.now();
        this.document = document;
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
                .filter(Transaction::isFinalized)
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

    /**
     * Atualiza automaticamente o status da parcela.
     */
    public void updateStatus() {
        if (isPaid()) {
            this.status = PaymentStatus.FINALIZED;
        } else {
            this.status = PaymentStatus.OPEN;
        }
    }

    public Installment(BigDecimal amount, LocalDate dueDate, PaymentStatus status, User createdBy, Document document) {
        this.createdBy = createdBy;
        this.amount = amount;
        this.dueDate = dueDate;
        this.status = status;
        this.document = document;
    }
}
