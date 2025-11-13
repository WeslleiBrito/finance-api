CREATE TABLE payment_instrument (
    id UUID PRIMARY KEY,
    name VARCHAR(25) NOT NULL,
    is_global BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    instrument_nature VARCHAR(15) NOT NULL,
    payment_type VARCHAR(20) NOT NULL,
    created_by UUID NOT NULL,
    CONSTRAINT fk_payment_instrument_user FOREIGN KEY (created_by) REFERENCES users(id)
);
