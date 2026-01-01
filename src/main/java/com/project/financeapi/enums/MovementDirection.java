package com.project.financeapi.enums;

public enum MovementDirection {

    INFLOW {
        @Override
        public MovementDirection toggle() {
            return OUTFLOW;
        }
    },

    OUTFLOW {
        @Override
        public MovementDirection toggle() {
            return INFLOW;
        }
    };

    public abstract MovementDirection toggle();
}
