package com.project.financeapi.enums;


public enum CardBrandStatus {
    ACTIVE  {
        @Override
        public CardBrandStatus toggle() {
            return INACTIVE;
        }
    },
    INACTIVE {
        @Override
        public CardBrandStatus toggle() {
            return ACTIVE;
        }
    };

    public abstract CardBrandStatus toggle();
}
