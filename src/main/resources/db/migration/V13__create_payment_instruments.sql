CREATE TABLE payment_instrument (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(25) NOT NULL,
    is_global BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    instrument_nature VARCHAR(15) NOT NULL,
    created_by UUID NOT NULL,
    CONSTRAINT fk_payment_instrument_user FOREIGN KEY (created_by)
        REFERENCES users(id) ON DELETE CASCADE
);
