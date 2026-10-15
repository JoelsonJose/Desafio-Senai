CREATE TABLE transacoes (
    id SERIAL PRIMARY KEY,
    clube_id INTEGER NOT NULL REFERENCES clubes(id),
    jogador_id INTEGER NOT NULL REFERENCES jogadores(id),
    nome_jogador VARCHAR(100) NOT NULL,
    tipo VARCHAR(6) NOT NULL,
    valor DECIMAL(6,2) NOT NULL,
    taxa DECIMAL(6,2) NOT NULL,
    multa DECIMAL(6,2) NOT NULL DEFAULT 0.00,
    lucro DECIMAL(7,2),
    variacao_caixa DECIMAL(8,2) NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);