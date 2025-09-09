package com.project.financeapi.entity;

import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.enums.AccountType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "investment_account")
@Getter
@Setter
public class InvestmentAccount extends AccountBase {

    private BigDecimal riskLevel;

    public InvestmentAccount(User accountHolder, BigDecimal initialValue) {
        super(AccountType.INVESTMENT, accountHolder, initialValue);
    }

    public InvestmentAccount() {
    }
}
