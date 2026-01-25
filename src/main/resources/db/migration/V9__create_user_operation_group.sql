
CREATE TABLE user_operation_group (
    user_id UUID NOT NULL,
    operation_group_id UUID NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    PRIMARY KEY (user_id, operation_group_id),

    CONSTRAINT fk_uog_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_uog_group
        FOREIGN KEY (operation_group_id)
        REFERENCES operation_group(id)
        ON DELETE CASCADE
);
