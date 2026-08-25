-- Garante que a coluna existe e é do tipo texto
ALTER TABLE operation_type ADD COLUMN IF NOT EXISTS movement_direction VARCHAR(50);

-- Atualiza os registros comparando strings (com aspas simples)
UPDATE operation_type 
SET movement_direction = CASE 
    WHEN movement_type = 'PAYMENT' THEN 'OUTFLOW'
    ELSE 'INFLOW'
END;