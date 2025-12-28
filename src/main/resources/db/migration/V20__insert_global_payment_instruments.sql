-- 1. Cria a tabela filha genérica
CREATE TABLE IF NOT EXISTS public.simple_payment_instrument (
    id uuid NOT NULL,
    CONSTRAINT simple_payment_pkey PRIMARY KEY (id),
    CONSTRAINT fk_simple_payment_pai FOREIGN KEY (id) REFERENCES public.payment_instrument(id)
);

-- 2. Inserção de Dados (PAI + FILHO)

-- === DINHEIRO (CASH) ===
INSERT INTO payment_instrument (id, name, is_global, created_at, instrument_nature, payment_type, created_by, status)
VALUES ('11111111-1111-1111-1111-111111111111', 'Dinheiro', true, now(), 'PAYMENT', 'CASH', NULL, 'ACTIVE');

INSERT INTO simple_payment_instrument (id) VALUES ('11111111-1111-1111-1111-111111111111');


-- === PIX ===
INSERT INTO payment_instrument (id, name, is_global, created_at, instrument_nature, payment_type, created_by, status)
VALUES ('22222222-2222-2222-2222-222222222222', 'PIX', true, now(), 'PAYMENT', 'PIX', NULL, 'ACTIVE');

INSERT INTO simple_payment_instrument (id) VALUES ('22222222-2222-2222-2222-222222222222');


-- === TRANSFERÊNCIA (BANK_TRANSFER) ===
INSERT INTO payment_instrument (id, name, is_global, created_at, instrument_nature, payment_type, created_by, status)
VALUES ('33333333-3333-3333-3333-333333333333', 'Transferência Bancária', true, now(), 'PAYMENT', 'BANK_TRANSFER', NULL, 'ACTIVE');

INSERT INTO simple_payment_instrument (id) VALUES ('33333333-3333-3333-3333-333333333333');


-- === BOLETO ===
INSERT INTO payment_instrument (id, name, is_global, created_at, instrument_nature, payment_type, created_by, status)
VALUES ('44444444-4444-4444-4444-444444444444', 'Boleto Bancário', true, now(), 'PURCHASE', 'BOLETO', NULL, 'ACTIVE');

INSERT INTO simple_payment_instrument (id) VALUES ('44444444-4444-4444-4444-444444444444');