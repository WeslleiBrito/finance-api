-- 1. Remover a coluna de status estática da tabela principal
ALTER TABLE card_brand DROP COLUMN IF EXISTS status;

-- 2. Criar a nova tabela de Blacklist (Opt-out Dinâmico) para Bandeiras
CREATE TABLE deactivated_card_brands (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(128) NOT NULL,
    card_brand_id UUID NOT NULL,

    CONSTRAINT fk_deact_card_brand_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_deact_card_brand_ref
        FOREIGN KEY (card_brand_id) REFERENCES card_brand(id) ON DELETE CASCADE,

    -- Garante que o usuário não insira a mesma bandeira duas vezes na lista negra
    CONSTRAINT uq_deact_card_brand
        UNIQUE (user_id, card_brand_id)
);