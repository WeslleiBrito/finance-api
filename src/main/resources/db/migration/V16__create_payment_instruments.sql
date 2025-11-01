CREATE TABLE payment_instruments (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(25) NOT NULL,
    is_global BOOLEAN NOT NULL DEFAULT FALSE,
    created_by VARCHAR(36),
    created_at TIMESTAMP DEFAULT NOW() NOT NULL,

    CONSTRAINT fk_payment_instruments_user FOREIGN KEY (created_by)
        REFERENCES users(id) ON DELETE SET NULL
);
