-- ===============================================
-- TABELA DERIVADA: credit_card
-- ===============================================
CREATE TABLE credit_card (
    id UUID PRIMARY KEY,  -- mesmo ID da tabela card (herança via JOINED)
    credit_limit NUMERIC(19,2) NOT NULL,
    closing_day INTEGER NOT NULL CHECK (closing_day BETWEEN 1 AND 31),
    due_day INTEGER NOT NULL CHECK (due_day BETWEEN 1 AND 31),
    card_brand_id UUID NOT NULL,
    bank_id UUID,
    revolving_interest NUMERIC(10,4) DEFAULT 0 NOT NULL,
    fine NUMERIC(10,4) DEFAULT 0 NOT NULL,

    CONSTRAINT fk_credit_card_card FOREIGN KEY (id)
        REFERENCES card(id) ON DELETE CASCADE,

    CONSTRAINT fk_credit_card_brand FOREIGN KEY (card_brand_id)
        REFERENCES card_brand(id) ON DELETE SET NULL,

    CONSTRAINT fk_credit_card_bank FOREIGN KEY (bank_id)
        REFERENCES bank(id) ON DELETE SET NULL
);