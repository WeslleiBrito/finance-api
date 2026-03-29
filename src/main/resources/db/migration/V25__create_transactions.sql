CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    amount NUMERIC(19, 2) NOT NULL,
    discount NUMERIC(19, 3),
    interest NUMERIC(19, 3),
    fine NUMERIC(19, 3),
    observations VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    payment_date DATE NOT NULL,
    installment_id UUID,
    movement_direction VARCHAR(10) NOT NULL,
    movement_type VARCHAR(30) NOT NULL DEFAULT 'PAYMENT',
    reversal_of_transaction_id UUID,
    created_by VARCHAR(128) NOT NULL,
    account_id UUID NOT NULL,
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
        ON DELETE SET NULL,

    CONSTRAINT fk_transaction_reversal
        FOREIGN KEY (reversal_of_transaction_id)
        REFERENCES transactions(id) ON DELETE CASCADE,

    CONSTRAINT chk_reversal_not_self
        CHECK (
            reversal_of_transaction_id IS NULL
            OR reversal_of_transaction_id <> id
        )
);
