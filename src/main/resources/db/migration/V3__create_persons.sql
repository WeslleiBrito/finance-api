CREATE TABLE persons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    person_type VARCHAR(50),
    created_by VARCHAR(128) NOT NULL,
    CONSTRAINT fk_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE RESTRICT
);