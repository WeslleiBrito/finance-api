-- 1. Excluir as tabelas de junção antigas
DROP TABLE IF EXISTS user_operation_type CASCADE;
DROP TABLE IF EXISTS user_operation_group CASCADE;

-- 2. Remover a coluna de status estática das tabelas principais
ALTER TABLE operation_type DROP COLUMN IF EXISTS operation_status;
ALTER TABLE operation_group DROP COLUMN IF EXISTS operation_status;

-- 3. Criar as novas tabelas de Blacklist (Opt-out Dinâmico)
CREATE TABLE deactivated_operation_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(128) NOT NULL,
    operation_group_id UUID NOT NULL,

    CONSTRAINT fk_deact_op_group_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_deact_op_group_ref
        FOREIGN KEY (operation_group_id) REFERENCES operation_group(id) ON DELETE CASCADE,

    -- Garante que o usuário não insira o mesmo grupo duas vezes na lista negra
    CONSTRAINT uq_deact_op_group
        UNIQUE (user_id, operation_group_id)
);

CREATE TABLE deactivated_operation_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(128) NOT NULL,
    operation_type_id UUID NOT NULL,

    CONSTRAINT fk_deact_op_type_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_deact_op_type_ref
        FOREIGN KEY (operation_type_id) REFERENCES operation_type(id) ON DELETE CASCADE,

    -- Garante que o usuário não insira o mesmo tipo duas vezes na lista negra
    CONSTRAINT uq_deact_op_type
        UNIQUE (user_id, operation_type_id)
);