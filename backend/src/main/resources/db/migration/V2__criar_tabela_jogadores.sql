CREATE TABLE jogadores (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    posicao VARCHAR(3) NOT NULL,
    agilidade INTEGER NOT NULL,
    idade INTEGER NOT NULL,
    valor_base DECIMAL(6,2) NOT NULL,
    condicao VARCHAR(20) NOT NULL DEFAULT 'APTO',
    clube_id INTEGER REFERENCES clubes(id) ON DELETE SET NULL,
    velocidade INTEGER NOT NULL,
    finalizacao INTEGER NOT NULL,
    passe INTEGER NOT NULL,
    drible INTEGER NOT NULL,
    defesa INTEGER NOT NULL,
    fisico INTEGER NOT NULL,
    potencial INTEGER NOT NULL,
    excluido_em TIMESTAMP,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);