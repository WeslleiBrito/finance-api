package com.project.financeapi.enumSystem;

public enum StatusEntity {
    ACTIVE {
        @Override
        public StatusEntity toggle() {
            return INACTIVATED;
        }
    },

    INACTIVATED{
        @Override
        public StatusEntity toggle() {
            return ACTIVE;
        }
    };

    public abstract StatusEntity toggle();
}
