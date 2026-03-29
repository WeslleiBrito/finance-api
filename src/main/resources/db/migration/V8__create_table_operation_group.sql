
CREATE TABLE operation_group (
    id UUID PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    operation_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_by VARCHAR(128)

    CONSTRAINT chk_operation_group_origin
        CHECK (
            (is_system = TRUE AND created_by IS NULL)
            OR
            (is_system = FALSE AND created_by IS NOT NULL)
        ),

    CONSTRAINT uq_operation_group_system
        UNIQUE (name, is_system),

    CONSTRAINT uq_operation_group_user
        UNIQUE (created_by, name),

    CONSTRAINT fk_operation_group_user
        FOREIGN KEY (created_by)
        REFERENCES users(id)
);
