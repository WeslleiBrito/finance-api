package com.project.financeapi.entity.account;

import com.project.financeapi.entity.User;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.enums.AccountType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "wallet_account")
public class WalletAccount extends AccountBase {

    public WalletAccount(User accountHolder, BigDecimal initialValue) {
        super(AccountType.WALLET, accountHolder, initialValue);
    }

    public WalletAccount() {
    }
}
