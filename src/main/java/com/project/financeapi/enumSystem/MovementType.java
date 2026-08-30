package com.project.financeapi.enumSystem;

public enum MovementType {
    RECEIPT,
    PAYMENT,
    REVERSAL,           // estorno de uma transação existente
    MANUAL_ADJUSTMENT,
    TRANSFER
}