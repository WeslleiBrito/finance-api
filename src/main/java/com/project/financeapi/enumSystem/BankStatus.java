package com.project.financeapi.enumSystem;

public enum BankStatus {
    ACTIVE {
        @Override
        public BankStatus toggle(){
            return INACTIVE;
        };
    },
    INACTIVE {
        @Override
        public BankStatus toggle(){
            return ACTIVE;
        };
    };

    public abstract BankStatus toggle();
}
