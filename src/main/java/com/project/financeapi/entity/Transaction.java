package com.project.financeapi.entity;

import com.project.financeapi.dto.transaction.TransactionResponseDTO;
import com.project.financeapi.entity.base.PaymentInstrumentBase;

import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.enumSystem.MovementDirection;
import com.project.financeapi.enumSystem.MovementType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(precision = 19, scale = 3)
    private BigDecimal discount;

    @Column(precision = 19, scale = 3)
    private BigDecimal interest;

    @Column(precision = 19, scale = 3)
    private BigDecimal fine;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_direction", nullable = false, length = 10)
    private MovementDirection movementDirection;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    private MovementType movementType;

    @Column(length = 255)
    private String observations;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    /* ================= RELAÇÕES ================= */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id")
    private AccountBase account;


    /**
     * Origem natural da transação (fluxo financeiro)
     * Pode ser null APENAS para ajuste manual
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installment_id")
    private Installment installment;

    /**
     * Transação que está sendo estornada
     * Usado APENAS quando movementType = REVERSAL
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversal_of_transaction_id")
    private Transaction reversalOf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_instrument_id")
    private PaymentInstrumentBase paymentInstrument;

    public BigDecimal getEffectiveAmount() {
        BigDecimal total = amount;

        if (interest != null) {
            total = total.add(interest);
        }

        if (fine != null) {
            total = total.add(fine);
        }

        if (discount != null) {
            total = total.subtract(discount);
        }

        return total.max(BigDecimal.ZERO);
    }

    public TransactionResponseDTO toResponse() {
        return new TransactionResponseDTO(
                this.id,
                this.installment != null ? this.installment.getId() : null,
                this.account.getId(),
                this.paymentInstrument != null ? this.paymentInstrument.getId() : null,

                this.amount,
                this.interest,
                this.fine,
                this.discount,
                this.getEffectiveAmount(),

                this.movementType,
                this.movementDirection,

                this.reversalOf != null ? this.reversalOf.getId() : null,
                this.reversalOf != null,

                this.paymentDate,
                this.createdAt,

                this.observations
        );
    }

    /* ================= CONSTRUTORES DE DOMÍNIO ================= */

    public Transaction(
            BigDecimal amount,
            BigDecimal interest,
            BigDecimal fine,
            BigDecimal discount,
            MovementDirection direction,
            MovementType type,
            LocalDate paymentDate,
            User createdBy,
            AccountBase account,
            Installment installment,
            Transaction reversalOf,
            PaymentInstrumentBase instrument,
            String observations
    ) {
        this.amount = amount;
        this.interest = interest;
        this.fine = fine;
        this.discount = discount;
        this.movementDirection = direction;
        this.movementType = type;
        this.paymentDate = paymentDate;
        this.createdBy = createdBy;
        this.account = account;
        this.installment = installment;
        this.reversalOf = reversalOf;
        this.paymentInstrument = instrument;
        this.observations = observations;
        this.createdAt = LocalDateTime.now();
    }
}

