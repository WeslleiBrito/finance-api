package com.project.financeapi.entity;

import com.project.financeapi.entity.base.AccountBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(length = 255)
    private String description;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountBase account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversed_transaction_id")
    private Transaction reversedTransaction;

    public Transaction(AccountBase account, TransactionType type, BigDecimal amount, LocalDateTime createdAt, String description) {
        this.type = type;
        this.amount = amount;
        this.description = (description != null && !description.isBlank()) ? description : null;
        this.createdAt = createdAt;
        this.account = account;
    }

    public Transaction() {
    }
}
