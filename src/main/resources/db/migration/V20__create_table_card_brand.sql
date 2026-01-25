-- =========================================================
-- Tabela: card_flag
-- Descrição: Armazena as bandeiras de cartões (Visa, Elo, etc)
-- =========================================================

CREATE TABLE card_brand (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_global BOOLEAN NOT NULL DEFAULT FALSE,
    created_by UUID,
    created_at DATE NOT NULL DEFAULT CURRENT_DATE,

    CONSTRAINT fk_card_flag_user FOREIGN KEY (created_by)
        REFERENCES users(id) ON DELETE SET NULL
);