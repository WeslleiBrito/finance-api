CREATE TABLE emails (
    id VARCHAR(36) PRIMARY KEY,
    address VARCHAR(150) UNIQUE NOT NULL,
    created_by VARCHAR(36) NOT NULL,
    person_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_email_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id),
    CONSTRAINT fk_email_person
        FOREIGN KEY (person_id)
        REFERENCES persons(id)
);