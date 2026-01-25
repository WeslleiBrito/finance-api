CREATE TABLE account_base (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL, -- CHECKING, SAVINGS, CREDIT, WALLET, etc
    account_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',

    initial_value NUMERIC(19, 2) NOT NULL,
    creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    user_id UUID NOT NULL,

    -- Banco global
    bank_id UUID,

    CONSTRAINT fk_account_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_account_bank
        FOREIGN KEY (bank_id)
        REFERENCES bank(id)
        ON DELETE SET NULL,

    -- Garante que a conta pertença a UM e apenas UM banco
    CONSTRAINT ck_account_bank_exclusive
        CHECK (
            (bank_id IS NOT NULL)
        )
);
