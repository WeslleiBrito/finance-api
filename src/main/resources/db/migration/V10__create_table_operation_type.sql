
CREATE TABLE operation_type (
    id UUID PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    movement_type VARCHAR(20) NOT NULL, -- RECEIPT | PAYMENT | REVERSAL | MANUAL_ADJUSTMENT
    operation_group_id UUID NOT NULL,
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    operation_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_by VARCHAR(128)

    CONSTRAINT chk_operation_type_origin
        CHECK (
            (is_system = TRUE AND created_by IS NULL)
            OR
            (is_system = FALSE AND created_by IS NOT NULL)
        ),

    CONSTRAINT uq_operation_type_system
        UNIQUE (name, operation_group_id, is_system),

    CONSTRAINT uq_operation_type_user
        UNIQUE (created_by, name, operation_group_id),

    CONSTRAINT fk_operation_type_group
        FOREIGN KEY (operation_group_id)
        REFERENCES operation_group(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_operation_type_user
        FOREIGN KEY (created_by)
        REFERENCES users(id)
);
