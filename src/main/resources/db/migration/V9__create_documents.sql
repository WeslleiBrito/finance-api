CREATE TABLE documents (
    id VARCHAR(36) PRIMARY KEY,
    description VARCHAR(20) NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL,
    movement_type VARCHAR(50) NOT NULL,
    issue_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_by VARCHAR(36) NOT NULL,
    person_id VARCHAR(36) NOT NULL,
    account_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_document_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id),
    CONSTRAINT fk_document_person
        FOREIGN KEY (person_id)
        REFERENCES persons(id),
    CONSTRAINT fk_document_account
        FOREIGN KEY (account_id)
        REFERENCES account_base(id)
);