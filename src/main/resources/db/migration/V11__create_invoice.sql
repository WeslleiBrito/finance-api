CREATE TABLE invoice (
    id VARCHAR(36) PRIMARY KEY,
    total_amount NUMERIC(19, 2) NOT NULL,
    issue_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_by VARCHAR(36) NOT NULL,
    person_id VARCHAR(36) NOT NULL,
    account_id VARCHAR(36) NOT NULL,
    operation_type_id VARCHAR(36) NOT NULL,

    CONSTRAINT fk_invoice_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id),

    CONSTRAINT fk_invoice_person
        FOREIGN KEY (person_id)
        REFERENCES persons(id),

    CONSTRAINT fk_invoice_account
        FOREIGN KEY (account_id)
        REFERENCES account_base(id),

    CONSTRAINT fk_invoice_operation_type
        FOREIGN KEY (operation_type_id)
        REFERENCES operation_type(id)
);
