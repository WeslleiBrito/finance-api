package com.project.financeapi.enums;

public enum MovementType {
    RECEIPT,
    PAYMENT,
    REVERSAL,           // estorno de uma transação existente
    MANUAL_ADJUSTMENT   // ajuste manual explícito

}
