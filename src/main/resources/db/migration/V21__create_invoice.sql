CREATE TABLE invoice (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    total_amount NUMERIC(19, 2) NOT NULL,
    issue_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_by VARCHAR(128) NOT NULL,
    person_id UUID NOT NULL,
    account_id UUID NOT NULL,
    operation_type_id UUID NOT NULL,

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
