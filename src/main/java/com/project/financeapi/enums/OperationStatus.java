package com.project.financeapi.enums;

public enum OperationStatus {
    ACTIVE {
        @Override
        public OperationStatus toggle() {
            return INACTIVATED;
        }
    },

    INACTIVATED{
        @Override
        public OperationStatus toggle() {
            return ACTIVE;
        }
    };

    public abstract OperationStatus toggle();
}
