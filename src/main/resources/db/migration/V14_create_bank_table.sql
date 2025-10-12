CREATE TABLE bank (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(25) NOT NULL UNIQUE,
    code VARCHAR(10) UNIQUE, -- Ex: 001, 260, 077...
    is_global BOOLEAN DEFAULT FALSE, -- bancos padrões do sistema
    created_by VARCHAR(36), -- quem criou o banco (usuário opcional)
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_bank_user UNIQUE (created_by, name),
    CONSTRAINT fk_bank_user FOREIGN KEY (created_by) REFERENCES users(id)
);


INSERT INTO bank (id, name, code, is_global) VALUES
    -- Bancos tradicionais
    ('00000000-0000-0000-0000-000000000001', 'Banco do Brasil', '001', TRUE),
    ('00000000-0000-0000-0000-000000000002', 'Caixa Econômica Federal', '104', TRUE),
    ('00000000-0000-0000-0000-000000000003', 'Bradesco', '237', TRUE),
    ('00000000-0000-0000-0000-000000000004', 'Itaú Unibanco', '341', TRUE),
    ('00000000-0000-0000-0000-000000000005', 'Santander', '033', TRUE),

    -- Bancos digitais
    ('00000000-0000-0000-0000-000000000006', 'Nubank', '260', TRUE),
    ('00000000-0000-0000-0000-000000000007', 'Banco Inter', '077', TRUE),
    ('00000000-0000-0000-0000-000000000008', 'C6 Bank', '336', TRUE),
    ('00000000-0000-0000-0000-000000000009', 'PicPay Bank', '380', TRUE),
    ('00000000-0000-0000-0000-000000000010', 'BTG Pactual', '208', TRUE),

    -- Corretoras e plataformas de investimento
    ('00000000-0000-0000-0000-000000000011', 'XP Investimentos', '102', TRUE),
    ('00000000-0000-0000-0000-000000000012', 'Rico Investimentos', '707', TRUE),
    ('00000000-0000-0000-0000-000000000013', 'Modal Mais', '746', TRUE),
    ('00000000-0000-0000-0000-000000000014', 'Clear Corretora', '655', TRUE),
    ('00000000-0000-0000-0000-000000000015', 'NuInvest', '191', TRUE),
    ('00000000-0000-0000-0000-000000000016', 'Toro Investimentos', '352', TRUE)
ON CONFLICT (id) DO NOTHING;
