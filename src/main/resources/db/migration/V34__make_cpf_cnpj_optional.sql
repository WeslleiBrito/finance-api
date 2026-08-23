-- Remove a obrigatoriedade de CPF e CNPJ para permitir cadastros simplificados (Criação Rápida)
ALTER TABLE physical_person ALTER COLUMN cpf DROP NOT NULL;

ALTER TABLE legal_entity ALTER COLUMN cnpj DROP NOT NULL;