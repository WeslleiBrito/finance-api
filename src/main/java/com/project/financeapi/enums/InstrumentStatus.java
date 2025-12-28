package com.project.financeapi.enums;

public enum InstrumentStatus {
    ACTIVE,
    INACTIVE;

    public InstrumentStatus toggle() {
        return this == ACTIVE ? INACTIVE : ACTIVE;
    }
}

