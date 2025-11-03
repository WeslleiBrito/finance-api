CREATE TABLE emails (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    address VARCHAR(150) UNIQUE NOT NULL,
    created_by UUID NOT NULL,
    person_id UUID NOT NULL,
    CONSTRAINT fk_email_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id),
    CONSTRAINT fk_email_person
        FOREIGN KEY (person_id)
        REFERENCES persons(id)
);