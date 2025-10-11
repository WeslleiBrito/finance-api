package com.project.financeapi.enums;

public enum MovementType {
    INCOME,
    EXPENSE;

    public static MovementType fromTransactionType(TransactionType type) {
        return switch (type) {
            case DEPOSIT, TRANSFER_IN -> INCOME;
            case WITHDRAWAL, PAYMENT, TRANSFER_OUT -> EXPENSE;
        };
    }
}
