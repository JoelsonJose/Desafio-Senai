CREATE TABLE duelos_partida (
    id SERIAL PRIMARY KEY,
    partida_id INTEGER NOT NULL REFERENCES partidas(id) ON DELETE CASCADE,
    ordem INTEGER NOT NULL,
    clube_atacante_id INTEGER NOT NULL REFERENCES clubes(id),
    atacante_id INTEGER NOT NULL REFERENCES jogadores(id),
    defensor_id INTEGER NOT NULL REFERENCES jogadores(id),
    nome_atacante VARCHAR(100) NOT NULL,
    nome_defensor VARCHAR(100) NOT NULL,
    posicao_atacante VARCHAR(3) NOT NULL,
    posicao_defensor VARCHAR(3) NOT NULL,
    condicao_atacante VARCHAR(20) NOT NULL,
    condicao_defensor VARCHAR(20) NOT NULL,
    atributo_ataque VARCHAR(10) NOT NULL,
    atributo_defesa VARCHAR(10) NOT NULL,
    valor_ataque INTEGER NOT NULL,
    valor_defesa INTEGER NOT NULL,
    gol BOOLEAN NOT NULL
);