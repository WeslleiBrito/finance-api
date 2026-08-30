CREATE TABLE payment_instrument (
    id UUID PRIMARY KEY,
    name VARCHAR(25) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    instrument_nature VARCHAR(15) NOT NULL,
    payment_type VARCHAR(20) NOT NULL,
    status VARCHAR(15) NOT NULL DEFAULT 'ACTIVE',
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    created_by VARCHAR(128),
    CONSTRAINT fk_payment_instrument_user
        FOREIGN KEY (created_by) REFERENCES users(id)
);

