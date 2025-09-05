package com.project.financeapi.entity;

import com.project.financeapi.entity.base.TransactionType;

public class Transfer extends TransactionType {
    public Transfer() {
        super(TransactionTypeE.TRANSFER, MovementType.INPUT_EXIT);
    }
}
