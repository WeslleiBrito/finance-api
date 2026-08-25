-- Adiciona a coluna opcional para o par da transação
ALTER TABLE transactions
ADD COLUMN linked_transaction_id UUID;

-- Cria a chave estrangeira referenciando a própria tabela
ALTER TABLE transactions
ADD CONSTRAINT fk_linked_transaction
FOREIGN KEY (linked_transaction_id) REFERENCES transactions(id);