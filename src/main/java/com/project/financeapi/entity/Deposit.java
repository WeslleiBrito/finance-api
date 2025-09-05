package com.project.financeapi.entity;

import com.project.financeapi.entity.base.TransactionType;

public class Deposit extends TransactionType {

    public Deposit() {
        super(TransactionTypeE.DEPOSIT, MovementType.INPUT);
    }
}
