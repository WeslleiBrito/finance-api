package com.project.financeapi.enumSystem;

public enum MovementType {
    RECEIPT,
    PAYMENT,
    REVERSAL,           // estorno de uma transação existente
    MANUAL_ADJUSTMENT,
    TRANSFER,
    INVESTMENT_APPORT,            // Débito do caixa para compra de lote
    INVESTMENT_REDEEM_PRINCIPAL,  // Retorno de capital inicial resgatado
    INVESTMENT_REDEEM_PROFIT      // Entrada de rendimento líquido gerado
}