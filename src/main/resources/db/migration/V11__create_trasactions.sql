CREATE TABLE transactions (
    id VARCHAR(36) PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    movement_type VARCHAR(50) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    observations VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    payment_date DATE NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_by VARCHAR(36) NOT NULL,
    account_id VARCHAR(36) NOT NULL,
    installment_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_transaction_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id),
    CONSTRAINT fk_transaction_account
        FOREIGN KEY (account_id)
        REFERENCES account_base(id),
    CONSTRAINT fk_transaction_installment
        FOREIGN KEY (installment_id)
        REFERENCES installments(id)
);