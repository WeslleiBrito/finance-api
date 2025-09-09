CREATE TABLE phones (
    id VARCHAR(36) PRIMARY KEY,
    number VARCHAR(20) NOT NULL,
    type VARCHAR(50),
    created_by VARCHAR(36) NOT NULL,
    person_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_phone_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id),
    CONSTRAINT fk_phone_person
        FOREIGN KEY (person_id)
        REFERENCES persons(id)
);