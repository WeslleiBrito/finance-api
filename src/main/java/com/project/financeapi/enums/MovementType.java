package com.project.financeapi.enums;

public enum MovementType {
    CREDIT,
    DEBIT;

    public static MovementType fromTransactionType(TransactionType type) {
        return switch (type) {
            case DEPOSIT, TRANSFER_IN -> CREDIT;
            case WITHDRAWAL, PAYMENT, TRANSFER_OUT -> DEBIT;
        };
    }
}
