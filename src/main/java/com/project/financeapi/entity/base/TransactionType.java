package com.project.financeapi.entity.base;

import com.project.financeapi.entity.MovementType;
import com.project.financeapi.entity.TransactionTypeE;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class TransactionType {

    private TransactionTypeE nameTransactionType;

    private MovementType movementType;

    public TransactionType(TransactionTypeE nameTransactionType, MovementType movementType) {
        this.nameTransactionType = nameTransactionType;
        this.movementType = movementType;
    }

}
