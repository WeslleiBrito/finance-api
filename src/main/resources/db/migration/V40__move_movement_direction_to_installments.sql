-- 1. Remove a coluna da tabela operation_type
ALTER TABLE operation_type
DROP COLUMN movement_direction;

-- 2. Adiciona a coluna na tabela installments (como Enum de String)
ALTER TABLE installments
ADD COLUMN movement_direction VARCHAR(50);