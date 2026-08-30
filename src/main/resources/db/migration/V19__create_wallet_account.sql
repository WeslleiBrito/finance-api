CREATE TABLE wallet_account (
    id UUID PRIMARY KEY,
    CONSTRAINT fk_wallet_account FOREIGN KEY (id)
        REFERENCES account_base(id)
        ON DELETE CASCADE
);