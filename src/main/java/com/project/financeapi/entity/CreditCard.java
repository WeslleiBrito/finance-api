package com.project.financeapi.entity;

import com.project.financeapi.dto.bank.BankResponseDTO;
import com.project.financeapi.dto.card.cardBrand.CardBrandResponseDTO;
import com.project.financeapi.dto.payment.CreditCardDetailsDTO;
import com.project.financeapi.entity.base.PaymentInstrumentBase;
import com.project.financeapi.enums.CardStatus;
import com.project.financeapi.enums.InstrumentNature;
import com.project.financeapi.enums.PaymentType;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Data
@Entity
@Table(name = "credit_card")
@DiscriminatorValue("CREDIT")
public class CreditCard extends PaymentInstrumentBase {

    @Column(name = "card_holder_name", nullable = false, length = 60)
    private String cardHolderName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id")
    private Bank bank;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_brand_id")
    private CardBrand cardBrand;

    @Enumerated(EnumType.STRING)
    private CardStatus status = CardStatus.ACTIVE;

    @Column(name = "credit_limit", nullable = false)
    private BigDecimal creditLimit;

    @Column(name="closing_day", nullable = false)
    private Integer closingDay;

    @Column(name = "due_day", nullable = false)
    private Integer dueDay;

    @Column(name = "revolving_interest", nullable = false)
    private BigDecimal revolvingInterest;

    @Column(name = "fine", nullable = false)
    private BigDecimal fine;

    @OneToMany(mappedBy = "paymentInstrument", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Installment> installments = new ArrayList<>();

    @Override
    public List<Installment> getInstallments() {
        return installments;
    }
    public CreditCard(){}

    public CreditCard(
            String name,
            User createdBy,
            BigDecimal creditLimit,
            Integer closingDay,
            Integer dueDay,
            LocalDate expirationDate,
            CardBrand cardBrand,
            Bank bank,
            BigDecimal revolvingInterest,
            BigDecimal fine
    ) {
        super(name, createdBy, InstrumentNature.PURCHASE, PaymentType.CREDIT_CARD);
        this.creditLimit = creditLimit;
        this.closingDay = closingDay;
        this.dueDay = dueDay;
        this.revolvingInterest = revolvingInterest != null ? revolvingInterest : BigDecimal.ZERO;
        this.expirationDate = expirationDate;
        this.cardBrand = cardBrand;
        this.bank = bank;
        this.fine = fine != null ? fine : BigDecimal.ZERO;
        this.cardHolderName = name;
    }

    /**
     * Calcula o limite disponível com base nas parcelas em aberto.
     */
    @Transient
    public BigDecimal getAvailableLimit() {
        if (getInstallments() == null || getInstallments().isEmpty()) {
            return creditLimit;
        }

        BigDecimal totalEmAberto = getInstallments().stream()
                .map(Installment::getRemainingBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return creditLimit.subtract(totalEmAberto.max(BigDecimal.ZERO));
    }


    @Override
    public CreditCardDetailsDTO toDTO(){

        return new CreditCardDetailsDTO(
                this.getId(),
                this.getPaymentType(),
                this.getIsGlobal(),
                this.getCreatedAt(),
                this.getInstrumentNature(),
                this.getExpirationDate(),
                this.cardHolderName,
                this.closingDay,
                this.dueDay,
                this.creditLimit,
                this.getAvailableLimit(),
                this.revolvingInterest,
                this.fine,
                this.getStatus(),
                new CardBrandResponseDTO(
                        this.getCardBrand().getId(),
                        this.getCardBrand().getName(),
                        this.getCardBrand().getStatus(),
                        this.getCardBrand().isGlobal(),
                        this.getCardBrand().getCreatedAt()
                ),
                new BankResponseDTO(
                        this.getBank().getId(),
                        this.getBank().getName(),
                        this.getBank().getCode(),
                        this.getBank().getStatus()
                )
        );
    }
}
