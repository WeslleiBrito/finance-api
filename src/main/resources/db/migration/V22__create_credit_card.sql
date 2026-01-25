CREATE TABLE credit_card (
    id UUID PRIMARY KEY,
    card_holder_name VARCHAR(60) NOT NULL,
    bank_id UUID,
    expiration_date DATE,
    card_brand_id UUID,
    status VARCHAR(20) NOT NULL,
    credit_limit DECIMAL(19,2) NOT NULL,
    closing_day INT NOT NULL,
    due_day INT NOT NULL,
    revolving_interest DECIMAL(19,2) NOT NULL,
    fine DECIMAL(19,2) NOT NULL,
    CONSTRAINT fk_credit_card_payment_instrument FOREIGN KEY (id) REFERENCES payment_instrument(id),
    CONSTRAINT fk_credit_card_bank FOREIGN KEY (bank_id) REFERENCES bank(id),
    CONSTRAINT fk_credit_card_brand FOREIGN KEY (card_brand_id) REFERENCES card_brand(id)
);
