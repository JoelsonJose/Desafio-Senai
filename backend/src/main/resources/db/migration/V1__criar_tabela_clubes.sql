CREATE TABLE clubes (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(60) NOT NULL,
    sigla CHAR(3) NOT NULL,
    caixa DECIMAL(10,2) NOT NULL DEFAULT 300.00,
    formacao VARCHAR(5),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_clubes_sigla_unique ON clubes (sigla);