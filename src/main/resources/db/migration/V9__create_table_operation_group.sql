CREATE TABLE operation_group (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    created_by VARCHAR(36),
    is_global BOOLEAN DEFAULT FALSE,
    operation_group_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uq_operation_group_user UNIQUE (created_by, name),
    CONSTRAINT fk_operation_group_user FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Inserções padrão (grupos principais)
INSERT INTO operation_group (id, name, is_global) VALUES
    ('00000000-0000-0000-0000-000000000001', 'Moradia', TRUE),
    ('00000000-0000-0000-0000-000000000002', 'Alimentação', TRUE),
    ('00000000-0000-0000-0000-000000000003', 'Transporte', TRUE),
    ('00000000-0000-0000-0000-000000000004', 'Saúde', TRUE),
    ('00000000-0000-0000-0000-000000000005', 'Educação', TRUE),
    ('00000000-0000-0000-0000-000000000006', 'Lazer', TRUE),
    ('00000000-0000-0000-0000-000000000007', 'Investimentos', TRUE),
    ('00000000-0000-0000-0000-000000000008', 'Impostos e taxas', TRUE),
    ('00000000-0000-0000-0000-000000000009', 'Renda', TRUE),
    ('00000000-0000-0000-0000-000000000010', 'Outros', TRUE)
ON CONFLICT (id) DO NOTHING;