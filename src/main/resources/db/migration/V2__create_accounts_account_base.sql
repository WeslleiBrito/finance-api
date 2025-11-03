CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE account_base (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    type VARCHAR(50) NOT NULL,
    account_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    initial_value NUMERIC(19, 2) NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    user_id UUID NOT NULL,
    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);
