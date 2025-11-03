-- Ativar suporte a UUID (necessário para gen_random_uuid)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ========================
-- Tabelas filhas (herdam o ID)
-- ========================

CREATE TABLE checking_account (
    id UUID PRIMARY KEY,
    overdraft_limit NUMERIC(19,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_checking_account FOREIGN KEY (id)
        REFERENCES account_base(id)
        ON DELETE CASCADE
);

CREATE TABLE investment_account (
    id UUID PRIMARY KEY,
    risk_level NUMERIC(19,2),
    CONSTRAINT fk_investment_account FOREIGN KEY (id)
        REFERENCES account_base(id)
        ON DELETE CASCADE
);

CREATE TABLE payment_account (
    id UUID PRIMARY KEY,
    provider VARCHAR(255),
    CONSTRAINT fk_payment_account FOREIGN KEY (id)
        REFERENCES account_base(id)
        ON DELETE CASCADE
);

CREATE TABLE savings_account (
    id UUID PRIMARY KEY,
    interest_rate NUMERIC(19,4) NOT NULL DEFAULT 0.005,
    CONSTRAINT fk_savings_account FOREIGN KEY (id)
        REFERENCES account_base(id)
        ON DELETE CASCADE
);

CREATE TABLE wallet_account (
    id UUID PRIMARY KEY,
    CONSTRAINT fk_wallet_account FOREIGN KEY (id)
        REFERENCES account_base(id)
        ON DELETE CASCADE
);
