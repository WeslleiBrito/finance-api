CREATE TABLE credit_card (
    id VARCHAR(36) PRIMARY KEY,
    credit_limit NUMERIC(19,2) NOT NULL,
    closing_day INTEGER NOT NULL CHECK (closing_day BETWEEN 1 AND 31),
    due_day INTEGER NOT NULL CHECK (due_day BETWEEN 1 AND 31),

    CONSTRAINT fk_credit_card_card FOREIGN KEY (id)
        REFERENCES card(id) ON DELETE CASCADE
);
