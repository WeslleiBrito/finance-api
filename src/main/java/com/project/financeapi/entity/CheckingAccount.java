package com.project.financeapi.entity;

import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.enums.AccountType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "checking_account")
@Getter
@Setter
public class CheckingAccount extends AccountBase {
    private BigDecimal overdraftLimit = BigDecimal.ZERO; // limite do cheque especial

    public CheckingAccount(User accountHolder) {
        super(AccountType.CHECKING, accountHolder);
    }
}
