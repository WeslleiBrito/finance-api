
CREATE TABLE user_operation_type (
    user_id VARCHAR(128) NOT NULL,
    operation_type_id UUID NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    PRIMARY KEY (user_id, operation_type_id),

    CONSTRAINT fk_uot_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_uot_type
        FOREIGN KEY (operation_type_id)
        REFERENCES operation_type(id)
        ON DELETE CASCADE
);
