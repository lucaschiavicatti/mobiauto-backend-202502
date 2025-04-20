CREATE TABLE cliente (
    id BIGSERIAL PRIMARY KEY,
    nome TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    telefone TEXT NOT NULL,
    revenda_id BIGINT NOT NULL,
    CONSTRAINT fk_cliente_revenda FOREIGN KEY (revenda_id) REFERENCES revenda(id)
);
