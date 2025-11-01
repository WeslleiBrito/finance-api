package com.project.financeapi.entity;

import com.project.financeapi.entity.base.PaymentInstrumentBase;
import com.project.financeapi.enums.MovementType;
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
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "observations")
    private String observations;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false, updatable = false)
    private LocalDate settlementDate;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate = LocalDate.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountBase account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installment_id")
    private Installment installment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_instrument_id")
    private PaymentInstrumentBase paymentInstrument;


    @Column(name = "is_reversed", nullable = false)
    private boolean reversed = false;


    public Transaction() {
    }

    public Transaction(
            User createdBy,
            AccountBase account,
            MovementType type,
            BigDecimal amount,
            PaymentInstrumentBase paymentInstrument,
            LocalDate settlementDate,
            String observations
    ) {
        this.account = account;
        this.amount = amount;
        this.settlementDate = settlementDate != null ? settlementDate : LocalDate.now();
        this.observations = (observations != null && !observations.isBlank()) ? observations : null;
        this.movementType = type;
        this.createdBy = createdBy;
        this.paymentInstrument = paymentInstrument;
    }

}
