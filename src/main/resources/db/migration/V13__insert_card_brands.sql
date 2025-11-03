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

-- =========================================================
-- Inserções padrão: bandeiras globais e nacionais
-- =========================================================

-- Bandeiras Globais
INSERT INTO card_brand (id, name, status, is_global)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'Visa', 'ACTIVE', TRUE),
    ('00000000-0000-0000-0000-000000000002', 'Mastercard', 'ACTIVE', TRUE),
    ('00000000-0000-0000-0000-000000000003', 'American Express', 'ACTIVE', TRUE),
    ('00000000-0000-0000-0000-000000000004', 'Diners Club', 'ACTIVE', TRUE),
    ('00000000-0000-0000-0000-000000000005', 'Discover', 'ACTIVE', TRUE),
    ('00000000-0000-0000-0000-000000000006', 'JCB', 'ACTIVE', TRUE),
    ('00000000-0000-0000-0000-000000000007', 'UnionPay', 'ACTIVE', TRUE);

-- Bandeiras Nacionais (Brasil)
INSERT INTO card_brand (id, name, status, is_global)
VALUES
    ('00000000-0000-0000-0000-000000000008', 'Elo', 'ACTIVE', TRUE),
    ('00000000-0000-0000-0000-000000000009', 'Hipercard', 'ACTIVE', TRUE),
    ('00000000-0000-0000-0000-000000000010', 'Banricompras', 'ACTIVE', TRUE);
