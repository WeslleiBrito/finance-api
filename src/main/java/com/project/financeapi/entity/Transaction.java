package com.project.financeapi.entity;

import com.project.financeapi.entity.base.PaymentInstrumentBase;
import com.project.financeapi.enums.MovementType;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.interfaces.PaymentInstrument;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @Column(length = 36)
    @Setter(AccessLevel.PRIVATE)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "observations")
    private String observations = null;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, updatable = false)
    private LocalDate paymentDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountBase account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "installment_id", nullable = false)
    private Installment installment;

    // Mapeamento genérico, aceitando qualquer instrumento que implemente PaymentInstrument
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = PaymentInstrumentBase.class)
    @JoinColumn(name = "payment_instrument_id")
    private PaymentInstrument paymentInstrument;

    @Column(name = "is_reversed", nullable = false)
    private Boolean isReversed;

    public Transaction() {
    }

    public Transaction(
            User createdBy,
            AccountBase account,
            Installment installment,
            BigDecimal amount,
            LocalDate paymentDate,
            Boolean isReversed,
            String observations
    ) {
        this.installment = installment;
        this.account = account;
        this.amount = amount;
        this.paymentDate = paymentDate != null ? paymentDate : LocalDate.now();
        this.observations = (observations != null && !observations.isBlank()) ? observations : null;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.isReversed = isReversed != null ? isReversed : false;
    }

}
