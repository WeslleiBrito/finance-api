CREATE TABLE payment_account (
    id UUID PRIMARY KEY,
    provider VARCHAR(255),
    CONSTRAINT fk_payment_account FOREIGN KEY (id)
        REFERENCES account_base(id)
        ON DELETE CASCADE
);