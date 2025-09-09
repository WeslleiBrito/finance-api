
-- PhysicalPerson
CREATE TABLE physical_person (
    id VARCHAR(36) PRIMARY KEY,
    nick_name VARCHAR(255) NOT NULL,
    cpf VARCHAR(255) NOT NULL,
    CONSTRAINT fk_physical_person
        FOREIGN KEY (id)
        REFERENCES persons(id)
        ON DELETE CASCADE
);

--legalEntity
CREATE TABLE legal_entity (
    id VARCHAR(36) PRIMARY KEY,
    trade_name VARCHAR(255) NOT NULL,
    cnpj VARCHAR(255) NOT NULL,
    CONSTRAINT fk_legal_entity
        FOREIGN KEY (id)
        REFERENCES persons(id)
        ON DELETE CASCADE
);