-- CheckingAccount
CREATE TABLE checking_account (
    id VARCHAR(36) PRIMARY KEY,
    overdraft_limit NUMERIC(19,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_checking_account FOREIGN KEY (id) REFERENCES account_base(id) ON DELETE CASCADE
);

-- InvestmentAccount
CREATE TABLE investment_account (
    id VARCHAR(36) PRIMARY KEY,
    risk_level NUMERIC(19,2),
    CONSTRAINT fk_investment_account FOREIGN KEY (id) REFERENCES account_base(id) ON DELETE CASCADE
);

-- PaymentAccount
CREATE TABLE payment_account (
    id VARCHAR(36) PRIMARY KEY,
    provider VARCHAR(255),
    CONSTRAINT fk_payment_account FOREIGN KEY (id) REFERENCES account_base(id) ON DELETE CASCADE
);

-- SavingsAccount
CREATE TABLE savings_account (
    id VARCHAR(36) PRIMARY KEY,
    interest_rate NUMERIC(19,4) NOT NULL DEFAULT 0.005,
    CONSTRAINT fk_savings_account FOREIGN KEY (id) REFERENCES account_base(id) ON DELETE CASCADE
);

-- WalletAccount
CREATE TABLE wallet_account (
    id VARCHAR(36) PRIMARY KEY,
    CONSTRAINT fk_wallet_account FOREIGN KEY (id) REFERENCES account_base(id) ON DELETE CASCADE
);
