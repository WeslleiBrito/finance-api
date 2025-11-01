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

    @Column(name = "available_limit", nullable = false)
    private BigDecimal availableLimit;


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
}
