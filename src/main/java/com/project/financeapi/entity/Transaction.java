package com.project.financeapi.entity;

import com.project.financeapi.enums.MovementType;
import com.project.financeapi.enums.PaymentStatus;
import com.project.financeapi.enums.TransactionType;
import com.project.financeapi.entity.base.AccountBase;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @Column(length = 36)
    @Setter(AccessLevel.PRIVATE)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "observations")
    private String observations;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false, updatable = false)
    private LocalDate paymentDate;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate = LocalDate.now();

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate = LocalDate.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountBase account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "installment_id", nullable = false)
    private Installment installment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.OPEN;

    public Transaction() {
    }

    public Transaction(
            User createdBy,
            AccountBase account,
            TransactionType type,
            BigDecimal amount,
            LocalDate issueDate,
            LocalDate dueDate,
            LocalDate paymentDate,
            String observations
    ) {
        this.account = account;
        this.type = type;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.issueDate = issueDate != null ? issueDate : LocalDate.now();
        this.dueDate = dueDate != null ? dueDate : LocalDate.now();
        this.paymentDate = paymentDate != null ? paymentDate : LocalDate.now();
        this.observations = (observations != null && !observations.isBlank()) ? observations : null;

        this.movementType = MovementType.fromTransactionType(type);
        this.createdBy = createdBy;
    }


    public boolean isFinalized() {
        return this.status == PaymentStatus.FINALIZED;
    }

    public boolean isOpen() {
        return this.status == PaymentStatus.OPEN;
    }

    public void cancel() {
        if (this.isFinalized()) {
            throw new IllegalStateException("Não é possível cancelar uma transação finalizada");
        }
        this.status = PaymentStatus.CANCELLED;
    }

    public void finalizeTransaction() {
        if (this.status != PaymentStatus.OPEN) {
            throw new IllegalStateException("Só é possível finalizar transações abertas");
        }
        this.status = PaymentStatus.FINALIZED;
    }
}
