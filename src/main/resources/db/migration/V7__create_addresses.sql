CREATE TABLE addresses (
    id VARCHAR(36) PRIMARY KEY,
    street VARCHAR(150) NOT NULL,
    number VARCHAR(10) NOT NULL,
    neighborhood VARCHAR(20) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(20) NOT NULL,
    zip_code VARCHAR(10) NOT NULL,
    complement VARCHAR(50),
    created_by VARCHAR(36) NOT NULL,
    person_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_address_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id),
    CONSTRAINT fk_address_person
        FOREIGN KEY (person_id)
        REFERENCES persons(id)
);