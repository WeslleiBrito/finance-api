-- Remove a coluna física de status para adotar o cálculo dinâmico
ALTER TABLE invoice DROP COLUMN IF EXISTS status;