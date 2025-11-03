CREATE TABLE card (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    number VARCHAR(16),
    bank_id UUID,
    expiration_date DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,
    card_brand_id UUID,
    card_type VARCHAR(20) NOT NULL,
    name VARCHAR(25) NOT NULL,
    is_global BOOLEAN DEFAULT FALSE NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP DEFAULT NOW() NOT NULL,

    CONSTRAINT fk_cards_user FOREIGN KEY (created_by)
        REFERENCES users(id) ON DELETE SET NULL,

    CONSTRAINT fk_cards_bank FOREIGN KEY (bank_id)
        REFERENCES bank(id) ON DELETE SET NULL,

    CONSTRAINT fk_cards_brand FOREIGN KEY (card_brand_id)
        REFERENCES card_brand(id) ON DELETE SET NULL
);
