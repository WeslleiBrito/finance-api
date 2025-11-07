package com.project.financeapi.entity.account;

import com.project.financeapi.entity.Bank;
import com.project.financeapi.entity.User;
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

    public InvestmentAccount(User accountHolder, String name, BigDecimal initialValue, Bank bank, BigDecimal riskLevel) {
        super(AccountType.INVESTMENT, accountHolder, name, initialValue, bank);
        this.riskLevel = riskLevel;
    }

    public InvestmentAccount() {
    }
}
