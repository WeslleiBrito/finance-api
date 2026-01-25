CREATE TABLE savings_account (
    id UUID PRIMARY KEY,
    interest_rate NUMERIC(19,2),
    CONSTRAINT fk_savings_account FOREIGN KEY (id)
        REFERENCES account_base(id)
        ON DELETE CASCADE
);