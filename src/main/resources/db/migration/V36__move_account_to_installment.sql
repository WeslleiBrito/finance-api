-- 1. Remove a conta da Fatura
ALTER TABLE invoice DROP CONSTRAINT fk_invoice_account;
ALTER TABLE invoice DROP COLUMN account_id;

-- 2. Adiciona a conta na Parcela
ALTER TABLE installments ADD COLUMN account_id UUID;
ALTER TABLE installments ADD CONSTRAINT fk_installment_account
    FOREIGN KEY (account_id) REFERENCES account_base(id) ON DELETE SET NULL;