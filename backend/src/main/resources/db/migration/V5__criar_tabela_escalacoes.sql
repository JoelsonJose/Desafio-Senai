CREATE TABLE escalacoes (
    clube_id INTEGER NOT NULL REFERENCES clubes(id) ON DELETE CASCADE,
    jogador_id INTEGER NOT NULL REFERENCES jogadores(id) ON DELETE CASCADE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (clube_id, jogador_id)
);

CREATE UNIQUE INDEX idx_escalacoes_jogador_unique ON escalacoes (jogador_id);