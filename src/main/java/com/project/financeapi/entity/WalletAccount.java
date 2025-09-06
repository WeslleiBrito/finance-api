package com.project.financeapi.entity;

import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.enums.AccountType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "wallet_account")
public class WalletAccount extends AccountBase {

    public WalletAccount(User accountHolder) {
        super(AccountType.WALLET, accountHolder);
    }
}
