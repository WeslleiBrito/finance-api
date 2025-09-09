CREATE TABLE account_base (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    account_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVATED',
    initial_value NUMERIC(19, 2) NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);