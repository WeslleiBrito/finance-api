package com.project.financeapi.enumSystem;

public enum FixedIncomeType {
    CDB(false),
    LCI(true),
    LCA(true),
    CRI(true),
    CRA(true),
    TESOURO_DIRETO(false),
    DEBENTURE_COMUM(false),
    DEBENTURE_INCENTIVADA(true),
    RDB(false);

    private final boolean taxExempt;

    FixedIncomeType(boolean taxExempt) {
        this.taxExempt = taxExempt;
    }

    public boolean isTaxExempt() {
        return taxExempt;
    }
}