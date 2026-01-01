-- 1. Remover flag incorreta de estorno
ALTER TABLE transactions
DROP COLUMN is_reversed;

-- 2. Permitir transações sem parcela (ex: ajuste manual)
ALTER TABLE transactions
ALTER COLUMN installment_id DROP NOT NULL;

-- 3. Adicionar direção do movimento
ALTER TABLE transactions
ADD COLUMN movement_direction VARCHAR(10) NOT NULL;

-- 4. Adicionar tipo da transação
ALTER TABLE transactions
ADD COLUMN transaction_type VARCHAR(30) NOT NULL;

-- 5. Relacionamento de estorno com transação original
ALTER TABLE transactions
ADD COLUMN reversal_of_transaction_id UUID;

-- 6. FK do estorno
ALTER TABLE transactions
ADD CONSTRAINT fk_transaction_reversal
FOREIGN KEY (reversal_of_transaction_id)
REFERENCES transactions(id);

-- 7. Garantir que não exista estorno de estorno
ALTER TABLE transactions
ADD CONSTRAINT chk_reversal_not_self
CHECK (
    reversal_of_transaction_id IS NULL
    OR reversal_of_transaction_id <> id
);

-- 8. Adicionar desconto
ALTER TABLE transactions
ADD COLUMN discount NUMERIC(19, 3);

-- 9. Adicionar interest
ALTER TABLE transactions
ADD COLUMN interest NUMERIC(19, 3);

-- 10. Adicionar fine
ALTER TABLE transactions
ADD COLUMN fine NUMERIC(19, 3);