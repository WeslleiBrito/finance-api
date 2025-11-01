CREATE TABLE payment_method (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,
    is_global BOOLEAN DEFAULT FALSE NOT NULL,
    created_by VARCHAR(36),
    created_at TIMESTAMP DEFAULT NOW() NOT NULL,
    CONSTRAINT fk_payment_method_user FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Formas padrão
INSERT INTO payment_method (id, name, is_global)
VALUES
    ('00000000-0000-0000-0000-000000000101', 'Dinheiro', TRUE),
    ('00000000-0000-0000-0000-000000000102', 'Cartão de crédito', TRUE),
    ('00000000-0000-0000-0000-000000000103', 'Cartão de débito', TRUE),
    ('00000000-0000-0000-0000-000000000104', 'PIX', TRUE),
    ('00000000-0000-0000-0000-000000000105', 'Transferência', TRUE),
    ('00000000-0000-0000-0000-000000000106', 'Cheque', TRUE);
