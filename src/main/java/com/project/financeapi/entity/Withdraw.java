package com.project.financeapi.entity;

import com.project.financeapi.entity.base.TransactionType;

public class Withdraw extends TransactionType {

    public Withdraw() {
        super(TransactionTypeE.WITHDRAW, MovementType.EXIT);
    }
}
