CREATE TABLE phones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    number VARCHAR(20) NOT NULL,
    type VARCHAR(50),
    created_by VARCHAR(128) NOT NULL,
    person_id UUID NOT NULL,
    CONSTRAINT fk_phone_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id),
    CONSTRAINT fk_phone_person
        FOREIGN KEY (person_id)
        REFERENCES persons(id)
);