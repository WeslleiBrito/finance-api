CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    amount NUMERIC(19, 2) NOT NULL,
    observations VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    payment_date DATE NOT NULL,
    is_reversed BOOLEAN NOT NULL DEFAULT FALSE,
    created_by UUID NOT NULL,
    account_id UUID NOT NULL,
    installment_id UUID NOT NULL,
    payment_instrument_id UUID,
    instrument_type VARCHAR(30) NOT NULL DEFAULT 'GENERIC', -- CARD | PAYMENT_INSTRUMENT

    CONSTRAINT fk_transaction_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT fk_transaction_account
        FOREIGN KEY (account_id)
        REFERENCES account_base(id) ON DELETE CASCADE,

    CONSTRAINT fk_transaction_installment
        FOREIGN KEY (installment_id)
        REFERENCES installments(id) ON DELETE CASCADE,

    CONSTRAINT fk_transaction_payment_instrument
        FOREIGN KEY (payment_instrument_id)
        REFERENCES payment_instrument(id)
        ON DELETE SET NULL
);
