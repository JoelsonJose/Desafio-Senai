CREATE TABLE contratos (
    id SERIAL PRIMARY KEY,
    jogador_id INTEGER NOT NULL REFERENCES jogadores(id),
    clube_id INTEGER NOT NULL REFERENCES clubes(id),
    valor_compra DECIMAL(6,2) NOT NULL,
    fidelidade_ate TIMESTAMP NOT NULL,
    situacao VARCHAR(12) NOT NULL DEFAULT 'ATIVO',
    iniciado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    encerrado_em TIMESTAMP
);