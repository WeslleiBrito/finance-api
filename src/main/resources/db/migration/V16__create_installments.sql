DROP TABLE IF EXISTS installments CASCADE;

CREATE TABLE installments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    amount NUMERIC(19, 2) NOT NULL,
    due_date DATE NOT NULL,
    movement_type VARCHAR(50) NOT NULL,
    created_at DATE NOT NULL DEFAULT NOW(),
    parcel_number INTEGER NOT NULL,
    created_by UUID NOT NULL,
    invoice_id UUID NOT NULL,
    payment_instrument UUID NULL,
    CONSTRAINT fk_installment_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_installment_invoice
        FOREIGN KEY (invoice_id)
        REFERENCES invoice(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_installment_payment_instrument
        FOREIGN KEY (payment_instrument)
        REFERENCES payment_instrument(id)
        ON DELETE SET NULL
);
