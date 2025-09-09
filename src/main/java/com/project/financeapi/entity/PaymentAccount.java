package com.project.financeapi.entity;

import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.enums.AccountType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_account")
@Getter
@Setter
public class PaymentAccount extends AccountBase {
    private String provider;

    public PaymentAccount(User accountHolder, BigDecimal initialValue) {
        super(AccountType.PAYMENT, accountHolder, initialValue);
    }

    public PaymentAccount() {
    }
}
