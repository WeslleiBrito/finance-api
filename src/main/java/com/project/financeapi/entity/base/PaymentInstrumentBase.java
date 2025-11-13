package com.project.financeapi.entity.base;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.project.financeapi.dto.payment.PaymentMethodDetailsDTO;
import com.project.financeapi.entity.Transaction;
import com.project.financeapi.entity.User;
import com.project.financeapi.enums.InstrumentNature;
import com.project.financeapi.enums.PaymentType;
import com.project.financeapi.interfaces.PaymentInstrument;
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
@Table(name = "payment_instrument")
@Getter
@Setter
@NoArgsConstructor
public abstract class PaymentInstrumentBase implements PaymentInstrument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private UUID id;

    @Column(nullable = false, length = 25)
    private String name;

    @Column(nullable = false, name = "is_global")
    private Boolean isGlobal = false;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15, name="instrument_nature")
    private InstrumentNature instrumentNature;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

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


    public PaymentInstrumentBase(String name, User createdBy, InstrumentNature instrumentNature, PaymentType paymentType) {
        this.name = name;
        this.createdBy = createdBy;
        this.instrumentNature = instrumentNature;
        this.paymentType = paymentType;
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

    public abstract PaymentMethodDetailsDTO toDTO();
}
