package com.project.financeapi.entity.base;

import com.project.financeapi.entity.Bank;
import com.project.financeapi.entity.CardBrand;
import com.project.financeapi.entity.User;
import com.project.financeapi.enums.CardStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;


@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "card")
@Data
public abstract class CardBase extends PaymentInstrumentBase {

    @Column(length = 16)
    private String number;

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


    public CardBase() {}
    public CardBase(String name, User createdBy) {
        super(name, createdBy);
    }


}
