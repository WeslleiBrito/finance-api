package com.project.financeapi.entity.payment;

import com.project.financeapi.entity.*;
import com.project.financeapi.entity.base.CardBase;
import com.project.financeapi.enums.InstrumentNature;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;



@Data
@Entity
@Table(name = "credit_card")
@DiscriminatorValue("CREDIT")
public class CreditCard extends CardBase {

    @Column(name = "credit_limit", nullable = false)
    private BigDecimal creditLimit;

    @Column(name="closing_day", nullable = false)
    private Integer closingDay;

    @Column(name = "due_day", nullable = false)
    private Integer dueDay;

    @Column(name ="card_brand_id", nullable = false)
    private CardBrand cardBrand;

    @Column(name = "bank_id")
    private Bank bank;

    @Column(name = "revolving_interest", nullable = false)
    private BigDecimal revolvingInterest;

    @Column(name = "fine", nullable = false)
    private BigDecimal fine;


    public CreditCard(
            String name,
            User createdBy,
            BigDecimal creditLimit,
            Integer closingDay,
            Integer dueDay,
            CardBrand cardBrand,
            Bank bank,
            BigDecimal revolvingInterest,
            BigDecimal fine
    ) {
        super(name, createdBy, InstrumentNature.PURCHASE);
        this.creditLimit = creditLimit;
        this.closingDay = closingDay;
        this.dueDay = dueDay;
        this.cardBrand = cardBrand;
        this.bank = bank;
        this.revolvingInterest = revolvingInterest != null ? revolvingInterest : BigDecimal.ZERO;
        this.fine = fine != null ? fine : BigDecimal.ZERO ;
    }

    /**
     * Calcula o limite disponível com base nas parcelas pendentes deste cartão.
     * Limite disponível = limite total - soma das parcelas em aberto.
     */
    @Transient
    public BigDecimal getAvailableLimit() {
        if (getTransactions() == null || getTransactions().isEmpty()) {
            return creditLimit;
        }

        // Soma das parcelas ainda em aberto associadas a este cartão
        BigDecimal totalEmAberto = getTransactions().stream()
                .filter(tx -> tx.getInstallment() != null)
                .filter(tx -> {
                    var installment = tx.getInstallment();
                    return installment.getRemainingBalance().compareTo(BigDecimal.ZERO) > 0;
                })
                .map(tx -> tx.getInstallment().getRemainingBalance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return creditLimit.subtract(totalEmAberto.max(BigDecimal.ZERO));
    }

}
