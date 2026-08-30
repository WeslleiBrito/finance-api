-- =========================================================
-- Descrição: Criação da tabela bank como master data
-- =========================================================

-- Extensão necessária para UUID (caso outras tabelas usem)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =========================================================
-- Tabela: bank
-- =========================================================
CREATE TABLE bank (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(10) UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

