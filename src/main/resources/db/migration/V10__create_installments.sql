CREATE TABLE installments (
    id VARCHAR(36) PRIMARY KEY,
    amount NUMERIC(19, 2) NOT NULL,
    due_date DATE NOT NULL,
    movement_type VARCHAR(50) NOT NULL,
    created_at DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    parcel_number INTEGER NOT NULL,
    created_by VARCHAR(36) NOT NULL,
    document_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_installment_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id),
    CONSTRAINT fk_installment_document
        FOREIGN KEY (document_id)
        REFERENCES documents(id)
);