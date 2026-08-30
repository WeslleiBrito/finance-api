CREATE TABLE IF NOT EXISTS public.simple_payment_instrument (
    id uuid NOT NULL,
    CONSTRAINT simple_payment_pkey PRIMARY KEY (id),
    CONSTRAINT fk_simple_payment_pai FOREIGN KEY (id) REFERENCES public.payment_instrument(id)
);