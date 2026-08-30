-- Remove a constraint antiga que bloqueava contas sem banco
ALTER TABLE account_base DROP CONSTRAINT ck_account_bank_exclusive;

-- Adiciona a nova constraint inteligente
-- 1. Se for WALLET, bank_id DEVE ser NULO.
-- 2. Se for qualquer outro tipo (CHECKING, SAVINGS, etc), bank_id NÃO PODE ser NULO.
ALTER TABLE account_base ADD CONSTRAINT ck_account_bank_exclusive
    CHECK (
        (type = 'WALLET' AND bank_id IS NULL)
        OR
        (type != 'WALLET' AND bank_id IS NOT NULL)
    );