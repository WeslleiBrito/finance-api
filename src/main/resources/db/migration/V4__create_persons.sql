CREATE TABLE persons (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    person_type VARCHAR(50),
    created_by VARCHAR(36) NOT NULL,
    CONSTRAINT fk_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE RESTRICT
);