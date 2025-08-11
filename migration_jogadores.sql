-- Migração para adicionar campo grupoId na tabela jogadores
-- IMPORTANTE: Execute este script antes de usar a nova versão do app

-- 1. Criar nova tabela com a estrutura correta
CREATE TABLE jogadores_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    grupoId INTEGER NOT NULL,
    nome TEXT NOT NULL,
    posicaoPrincipal TEXT NOT NULL,
    posicaoSecundaria TEXT,
    notaPosicaoPrincipal INTEGER NOT NULL,
    notaPosicaoSecundaria INTEGER,
    ativo INTEGER NOT NULL DEFAULT 1,
    disponivel INTEGER NOT NULL DEFAULT 1,
    totalJogos INTEGER NOT NULL DEFAULT 0,
    vitorias INTEGER NOT NULL DEFAULT 0,
    derrotas INTEGER NOT NULL DEFAULT 0,
    empates INTEGER NOT NULL DEFAULT 0,
    pontuacaoTotal INTEGER NOT NULL DEFAULT 0
);

-- 2. Migrar dados existentes (se houver) para o primeiro grupo (ID = 1)
-- Isso assumirá que existe pelo menos uma pelada criada
INSERT INTO jogadores_new (
    id, grupoId, nome, posicaoPrincipal, posicaoSecundaria,
    notaPosicaoPrincipal, notaPosicaoSecundaria, ativo, disponivel,
    totalJogos, vitorias, derrotas, empates, pontuacaoTotal
)
SELECT
    id,
    1 as grupoId,  -- Assign all existing players to the first group
    nome,
    posicaoPrincipal,
    posicaoSecundaria,
    notaPosicaoPrincipal,
    notaPosicaoSecundaria,
    ativo,
    disponivel,
    totalJogos,
    vitorias,
    derrotas,
    empates,
    pontuacaoTotal
FROM jogadores;

-- 3. Dropar tabela antiga e renomear a nova
DROP TABLE jogadores;
ALTER TABLE jogadores_new RENAME TO jogadores;

-- 4. Criar índice para melhor performance nas consultas por grupo
CREATE INDEX idx_jogadores_grupoId ON jogadores(grupoId);
