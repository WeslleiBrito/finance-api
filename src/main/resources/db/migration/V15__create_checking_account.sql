CREATE TABLE checking_account (
    id UUID PRIMARY KEY,
    overdraft_limit NUMERIC(19,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_checking_account FOREIGN KEY (id)
        REFERENCES account_base(id)
        ON DELETE CASCADE
);