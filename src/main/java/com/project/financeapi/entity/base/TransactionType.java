package com.project.financeapi.entity.base;

import com.project.financeapi.enums.MovementType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class TransactionType {

    private com.project.financeapi.enums.TransactionType nameTransactionType;

    private MovementType movementType;

    public TransactionType(com.project.financeapi.enums.TransactionType nameTransactionType, MovementType movementType) {
        this.nameTransactionType = nameTransactionType;
        this.movementType = movementType;
    }

}
