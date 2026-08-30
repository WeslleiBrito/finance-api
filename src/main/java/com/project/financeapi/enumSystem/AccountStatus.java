package com.project.financeapi.enumSystem;

public enum AccountStatus {
    ACTIVE {
        @Override
        public AccountStatus toggle() {
            return INACTIVATED;
        }
    },
    INACTIVATED {
        @Override
        public AccountStatus toggle() {
            return ACTIVE;
        }
    };

    public abstract AccountStatus toggle();
}
