-- Inicialização do banco de dados para RAG com PGVector

-- Habilitar extensão PGVector
CREATE EXTENSION IF NOT EXISTS vector;

-- Criar schema para embeddings se necessário
CREATE SCHEMA IF NOT EXISTS public;

-- A tabela 'embeddings' será criada automaticamente pelo Quarkus LangChain4j
-- Mas podemos criar índices adicionais para performance

-- Função para verificar se a extensão está instalada
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'vector') THEN
        RAISE EXCEPTION 'PGVector extension is not installed';
    END IF;
END $$;

-- Mensagem de sucesso
DO $$
BEGIN
    RAISE NOTICE 'Database initialized successfully with PGVector support';
END $$;

