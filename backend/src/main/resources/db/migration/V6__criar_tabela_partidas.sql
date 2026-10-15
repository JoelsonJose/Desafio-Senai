CREATE TABLE partidas (
    id SERIAL PRIMARY KEY,
    mandante_id INTEGER NOT NULL REFERENCES clubes(id),
    visitante_id INTEGER NOT NULL REFERENCES clubes(id),
    gols_mandante INTEGER NOT NULL,
    gols_visitante INTEGER NOT NULL,
    formacao_mandante VARCHAR(5) NOT NULL,
    formacao_visitante VARCHAR(5) NOT NULL,
    jogada_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);