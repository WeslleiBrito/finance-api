package com.project.financeapi.entity;

import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.enums.AccountType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "savings_account")
@Getter
@Setter
public class SavingsAccount extends AccountBase {
    private BigDecimal interestRate = BigDecimal.valueOf(0.005);

    public SavingsAccount(User accountHolder, BigDecimal initialValue) {
        super(AccountType.SAVINGS, accountHolder, initialValue);
    }

    public SavingsAccount() {
    }
}
