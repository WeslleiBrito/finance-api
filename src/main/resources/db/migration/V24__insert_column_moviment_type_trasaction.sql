ALTER TABLE transactions
ADD COLUMN movement_type VARCHAR(30);

UPDATE transactions
SET movement_type = 'PAYMENT';

ALTER TABLE transactions
ALTER COLUMN movement_type SET NOT NULL;
