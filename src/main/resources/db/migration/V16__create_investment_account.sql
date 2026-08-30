CREATE TABLE investment_account (
    id UUID PRIMARY KEY,
    risk_level NUMERIC(19,2),
    CONSTRAINT fk_investment_account FOREIGN KEY (id)
        REFERENCES account_base(id)
        ON DELETE CASCADE
);