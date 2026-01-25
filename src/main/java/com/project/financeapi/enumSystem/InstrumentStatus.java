package com.project.financeapi.enumSystem;

public enum InstrumentStatus {
    ACTIVE,
    INACTIVE;

    public InstrumentStatus toggle() {
        return this == ACTIVE ? INACTIVE : ACTIVE;
    }
}

