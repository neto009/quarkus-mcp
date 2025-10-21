# 🚀 Guia Completo - Sistema RAG Multi-Agentes com Quarkus

## 📋 Índice
1. [Visão Geral](#visão-geral)
2. [Pré-requisitos](#pré-requisitos)
3. [Configuração Inicial](#configuração-inicial)
4. [Estrutura do Projeto](#estrutura-do-projeto)
5. [Iniciando o Projeto](#iniciando-o-projeto)
6. [Endpoints e Testes](#endpoints-e-testes)
7. [Arquitetura Multi-Agentes](#arquitetura-multi-agentes)
8. [Troubleshooting](#troubleshooting)

---

## 🎯 Visão Geral

Este projeto implementa um sistema RAG (Retrieval-Augmented Generation) com múltiplos agentes de IA usando Quarkus, LangChain4j e PostgreSQL com pgvector. O sistema oferece duas abordagens diferentes para orquestração de agentes:

1. **LangChain4j Puro**: Implementação direta com LangChain4j
2. **MCP + gRPC**: Protocolo estruturado com comunicação gRPC entre agentes

### Funcionalidades Principais
- 📄 Upload e indexação de documentos (TXT, PDF, etc.)
- 🤖 Chat com IA usando RAG (busca semântica + LLM)
- 👥 Sistema multi-agentes com 4 agentes especializados
- 🔄 Comparação de abordagens (LangChain4j vs MCP+gRPC)
- 🗄️ Armazenamento vetorial com PostgreSQL + pgvector

---

## 📦 Pré-requisitos

### Software Necessário
- ☕ **Java 17 ou superior**
- 🐳 **Docker** e **Docker Compose**
- 📦 **Maven 3.8+**
- 🔧 **Git** (opcional)

### Verificar Instalação
```bash
# Verificar Java
java -version

# Verificar Maven
mvn -version

# Verificar Docker
docker --version
docker-compose --version
```

---

## ⚙️ Configuração Inicial

### Passo 1: Clonar/Acessar o Projeto
```bash
cd C:\Users\solan\Desktop\QUARKUS-MCP
```

### Passo 2: Configurar Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto (ou configure as variáveis no `application.properties`):

```properties
# OpenAI API Key (obrigatório)
QUARKUS_LANGCHAIN4J_OPENAI_API_KEY=sk-sua-chave-aqui

# Configuração do Banco de Dados
QUARKUS_DATASOURCE_USERNAME=postgres
QUARKUS_DATASOURCE_PASSWORD=postgres
QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/ragdb

# Porta da Aplicação
QUARKUS_HTTP_PORT=8080
```

### Passo 3: Subir o Banco de Dados PostgreSQL

O projeto usa PostgreSQL com a extensão pgvector para armazenamento de embeddings.

```bash
# Iniciar o Docker Compose (PostgreSQL + pgvector)
docker-compose up -d

# Verificar se o container está rodando
docker ps

# Ver logs do banco
docker-compose logs -f postgres
```

**Aguarde alguns segundos** para o banco inicializar completamente.

### Passo 4: Verificar Estrutura do Banco

O banco de dados será criado automaticamente pelo Quarkus com as seguintes tabelas:
- `documents`: Metadados dos documentos
- `embeddings`: Vetores de embeddings para busca semântica

---

## 📂 Estrutura do Projeto

```
QUARKUS-MCP/
├── src/
│   └── main/
│       ├── java/com/quarkus/rag/
│       │   ├── resource/          # Controllers REST
│       │   │   ├── ChatResource.java
│       │   │   ├── DocumentResource.java
│       │   │   ├── AgentResource.java
│       │   │   └── MCPComparisonResource.java
│       │   ├── service/           # Lógica de negócio
│       │   │   ├── RagService.java
│       │   │   ├── DocumentIngestionService.java
│       │   │   └── MultiAgentOrchestrator.java
│       │   ├── ai/                # Agentes de IA
│       │   │   ├── ChatService.java
│       │   │   └── agents/
│       │   │       ├── CoordinatorAgent.java
│       │   │       ├── DocumentAnalystAgent.java
│       │   │       ├── TechnicalExpertAgent.java
│       │   │       └── ValidatorAgent.java
│       │   ├── mcp/               # MCP + gRPC
│       │   │   └── service/
│       │   ├── entity/            # Entidades JPA
│       │   └── repository/        # Repositórios
│       ├── proto/                 # Definições gRPC
│       └── resources/
│           └── application.properties
├── documents/                     # Documentos de exemplo
├── docker-compose.yml
├── pom.xml
└── GUIA-COMPLETO.md              # Este arquivo
```

---

## 🚀 Iniciando o Projeto

### Modo Desenvolvimento (Dev Mode)

```bash
# Compilar e iniciar em modo desenvolvimento
mvn quarkus:dev
```

**Vantagens do Dev Mode:**
- ♻️ Hot reload automático
- 🐛 Debug facilitado
- 🔧 Configuração dinâmica

### Modo Produção

```bash
# Compilar o projeto
mvn clean package

# Executar o JAR
java -jar target/quarkus-app/quarkus-run.jar
```

### Verificar se a Aplicação Está Rodando

```bash
# Testar endpoint de saúde
curl http://localhost:8080/q/health

# Deve retornar status UP
```

---

## 🧪 Endpoints e Testes

### 1. 📄 Gerenciamento de Documentos

#### 1.1 Upload de Documento
Faz upload de um arquivo para indexação no sistema RAG.

**Endpoint:** `POST /api/documents/upload`

```bash
# Exemplo com arquivo TXT
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@C:\Users\solan\Desktop\QUARKUS-MCP\documents\Sobre.txt"

# Exemplo com arquivo PDF
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@C:\caminho\para\seu\arquivo.pdf"
```

**Resposta de Sucesso:**
```json
{
  "id": 1,
  "fileName": "Sobre.txt",
  "contentType": "text/plain",
  "fileSize": 1024,
  "uploadedAt": "2025-10-20T10:30:00",
  "processed": true
}
```

#### 1.2 Listar Todos os Documentos
Lista todos os documentos carregados no sistema.

**Endpoint:** `GET /api/documents`

```bash
curl http://localhost:8080/api/documents
```

**Resposta:**
```json
[
  {
    "id": 1,
    "fileName": "Sobre.txt",
    "contentType": "text/plain",
    "fileSize": 1024,
    "uploadedAt": "2025-10-20T10:30:00",
    "processed": true
  },
  {
    "id": 2,
    "fileName": "FAQ.txt",
    "contentType": "text/plain",
    "fileSize": 2048,
    "uploadedAt": "2025-10-20T11:00:00",
    "processed": true
  }
]
```

#### 1.3 Buscar Documento por ID
Obtém detalhes de um documento específico.

**Endpoint:** `GET /api/documents/{id}`

```bash
curl http://localhost:8080/api/documents/1
```

#### 1.4 Deletar Documento
Remove um documento do sistema.

**Endpoint:** `DELETE /api/documents/{id}`

```bash
curl -X DELETE http://localhost:8080/api/documents/1
```

**Resposta:** Status 204 (No Content) em caso de sucesso.

---

### 2. 💬 Chat Simples (RAG Básico)

Chat básico usando RAG sem multi-agentes.

**Endpoint:** `POST /api/chat`

```bash
# Pergunta simples
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d "{\"question\": \"O que é a empresa?\"}"

# Com limite de resultados customizado
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d "{\"question\": \"Quais são os planos disponíveis?\", \"maxResults\": 3}"
```

**Resposta:**
```json
{
  "answer": "A empresa é uma plataforma de inovação focada em sustentabilidade..."
}
```

---

### 3. 🤖 Multi-Agentes (LangChain4j)

Sistema avançado com 4 agentes especializados trabalhando em pipeline.

#### 3.1 Pergunta com Multi-Agentes (Resposta Completa)
Retorna todas as etapas do processamento multi-agente.

**Endpoint:** `POST /api/agents/ask`

```bash
curl -X POST http://localhost:8080/api/agents/ask \
  -H "Content-Type: application/json" \
  -d "{\"question\": \"Como funciona a segurança da plataforma?\"}"
```

**Resposta Completa:**
```json
{
  "question": "Como funciona a segurança da plataforma?",
  "documentAnalysis": "Análise do DocumentAnalystAgent: Os documentos relevantes indicam...",
  "technicalAnswer": "Resposta do TechnicalExpertAgent: A plataforma utiliza criptografia...",
  "validation": "Validação do ValidatorAgent: A resposta está correta e completa...",
  "finalAnswer": "Resposta final do CoordinatorAgent: A segurança da plataforma...",
  "agentsUsed": ["DocumentAnalyst", "TechnicalExpert", "Validator", "Coordinator"],
  "processingTimeMs": 2500
}
```

#### 3.2 Pergunta Simplificada (Apenas Resposta Final)
Retorna apenas a resposta final sem etapas intermediárias.

**Endpoint:** `POST /api/agents/ask-simple`

```bash
curl -X POST http://localhost:8080/api/agents/ask-simple \
  -H "Content-Type: application/json" \
  -d "{\"question\": \"Qual o custo dos planos?\"}"
```

**Resposta:**
```json
{
  "answer": "Os planos estão disponíveis em três categorias: Básico (R$ 29,90), Premium (R$ 79,90) e Enterprise (valor personalizado)..."
}
```

#### 3.3 Análise de Documento
Analisa um texto usando múltiplos agentes.

**Endpoint:** `POST /api/agents/analyze`

```bash
curl -X POST http://localhost:8080/api/agents/analyze \
  -H "Content-Type: application/json" \
  -d "{\"content\": \"Nossa plataforma oferece soluções inovadoras em sustentabilidade...\"}"
```

**Resposta:**
```json
{
  "summary": "Resumo gerado pelo DocumentAnalyst...",
  "technicalInsights": "Insights técnicos identificados...",
  "validationNotes": "Pontos de validação...",
  "overallAssessment": "Avaliação geral do documento..."
}
```

---

### 4. 🔄 MCP + gRPC (Protocolo Estruturado)

Abordagem alternativa usando MCP (Model Context Protocol) com gRPC.

#### 4.1 Pergunta com MCP
Processa pergunta usando protocolo MCP.

**Endpoint:** `POST /api/mcp/ask`

```bash
curl -X POST http://localhost:8080/api/mcp/ask \
  -H "Content-Type: application/json" \
  -d "{\"question\": \"Como entrar em contato com o suporte?\"}"
```

**Resposta:**
```json
{
  "finalAnswer": "Você pode entrar em contato com o suporte através...",
  "documentAnalysis": "Análise via MCP...",
  "technicalAnswer": "Resposta técnica via MCP...",
  "validation": "Validação via MCP...",
  "protocol": "MCP+gRPC",
  "processingTimeMs": 2100
}
```

#### 4.2 Comparação de Abordagens
Compara LangChain4j puro vs MCP+gRPC lado a lado.

**Endpoint:** `POST /api/mcp/compare`

```bash
curl -X POST http://localhost:8080/api/mcp/compare \
  -H "Content-Type: application/json" \
  -d "{\"question\": \"Quais são as funcionalidades principais?\"}"
```

**Resposta Comparativa:**
```json
{
  "langchain4jResult": {
    "approach": "LangChain4j Pure",
    "finalAnswer": "As funcionalidades principais incluem...",
    "documentAnalysis": "...",
    "technicalAnswer": "...",
    "validation": "...",
    "processingTimeMs": 2500,
    "error": null
  },
  "mcpResult": {
    "approach": "MCP + gRPC",
    "finalAnswer": "As funcionalidades principais incluem...",
    "documentAnalysis": "...",
    "technicalAnswer": "...",
    "validation": "...",
    "processingTimeMs": 2100,
    "error": null
  },
  "metrics": {
    "langchainDuration": 2500,
    "mcpDuration": 2100,
    "faster": "MCP+gRPC",
    "percentageDifference": 16.0
  }
}
```

---

## 👥 Arquitetura Multi-Agentes

### Os 4 Agentes Especializados

#### 1. 📊 DocumentAnalystAgent
- **Função:** Analisa documentos recuperados do RAG
- **Responsabilidade:** Extrair informações relevantes e contexto
- **Output:** Análise estruturada dos documentos

#### 2. 🔧 TechnicalExpertAgent
- **Função:** Responde perguntas técnicas especializadas
- **Responsabilidade:** Fornecer respostas detalhadas e técnicas
- **Output:** Resposta técnica fundamentada

#### 3. ✅ ValidatorAgent
- **Função:** Valida a qualidade das respostas
- **Responsabilidade:** Verificar precisão, completude e consistência
- **Output:** Relatório de validação

#### 4. 🎯 CoordinatorAgent
- **Função:** Coordena todos os agentes e gera resposta final
- **Responsabilidade:** Sintetizar informações e apresentar resposta coerente
- **Output:** Resposta final consolidada

### Pipeline de Processamento

```
Pergunta do Usuário
        ↓
[1] RAG - Busca Semântica
        ↓
[2] DocumentAnalystAgent → Analisa documentos recuperados
        ↓
[3] TechnicalExpertAgent → Gera resposta técnica
        ↓
[4] ValidatorAgent → Valida a resposta
        ↓
[5] CoordinatorAgent → Sintetiza resposta final
        ↓
Resposta ao Usuário
```

---

## 🧪 Cenário de Teste Completo

### Passo a Passo para Testar o Sistema

#### 1. Preparar Documentos
```bash
# Os documentos já estão na pasta documents/
dir C:\Users\solan\Desktop\QUARKUS-MCP\documents
```

#### 2. Fazer Upload dos Documentos
```bash
# Upload do arquivo Sobre.txt
curl -X POST http://localhost:8080/api/documents/upload -F "file=@documents\Sobre.txt"

# Upload do arquivo FAQ
curl -X POST http://localhost:8080/api/documents/upload -F "file=@documents\PerguntasFrequentes.txt"

# Upload de Planos
curl -X POST http://localhost:8080/api/documents/upload -F "file=@documents\PlanosAssinaturas.txt"

# Upload de Segurança
curl -X POST http://localhost:8080/api/documents/upload -F "file=@documents\Seguranca.txt"

# Upload de Suporte
curl -X POST http://localhost:8080/api/documents/upload -F "file=@documents\SuporteAtendimento.txt"

# Upload de Inovação
curl -X POST http://localhost:8080/api/documents/upload -F "file=@documents\InovacaoSustentabilidade.txt"
```

#### 3. Verificar Documentos Carregados
```bash
curl http://localhost:8080/api/documents
```

#### 4. Testar Chat Básico
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d "{\"question\": \"O que é a empresa?\"}"
```

#### 5. Testar Multi-Agentes
```bash
curl -X POST http://localhost:8080/api/agents/ask \
  -H "Content-Type: application/json" \
  -d "{\"question\": \"Como funciona a segurança da plataforma?\"}"
```

#### 6. Testar MCP
```bash
curl -X POST http://localhost:8080/api/mcp/ask \
  -H "Content-Type: application/json" \
  -d "{\"question\": \"Quais são os planos disponíveis?\"}"
```

#### 7. Comparar Abordagens
```bash
curl -X POST http://localhost:8080/api/mcp/compare \
  -H "Content-Type: application/json" \
  -d "{\"question\": \"Como entrar em contato com suporte?\"}"
```

---

## 🔍 Exemplos de Perguntas para Testar

### Perguntas Sobre a Empresa
```bash
curl -X POST http://localhost:8080/api/agents/ask \
  -H "Content-Type: application/json" \
  -d "{\"question\": \"O que faz a empresa?\"}"

curl -X POST http://localhost:8080/api/agents/ask \
  -H "Content-Type: application/json" \
  -d "{\"question\": \"Qual é a missão da empresa?\"}"
```

### Perguntas Sobre Planos
```bash
curl -X POST http://localhost:8080/api/agents/ask \
  -H "Content-Type: application/json" \
  -d "{\"question\": \"Quais planos estão disponíveis?\"}"

curl -X POST http://localhost:8080/api/agents/ask \
  -H "Content-Type: application/json" \
  -d "{\"question\": \"Qual a diferença entre os planos?\"}"
```

### Perguntas Sobre Segurança
```bash
curl -X POST http://localhost:8080/api/agents/ask \
  -H "Content-Type: application/json" \
  -d "{\"question\": \"Como a plataforma protege meus dados?\"}"

curl -X POST http://localhost:8080/api/agents/ask \
  -H "Content-Type: application/json" \
  -d "{\"question\": \"Quais certificações de segurança vocês possuem?\"}"
```

### Perguntas Sobre Suporte
```bash
curl -X POST http://localhost:8080/api/agents/ask \
  -H "Content-Type: application/json" \
  -d "{\"question\": \"Como falar com o suporte?\"}"

curl -X POST http://localhost:8080/api/agents/ask \
  -H "Content-Type: application/json" \
  -d "{\"question\": \"Qual o horário de atendimento?\"}"
```

---

## 🛠️ Troubleshooting

### Problema: Aplicação não inicia

**Sintomas:** Erro ao executar `mvn quarkus:dev`

**Soluções:**
```bash
# 1. Verificar se o PostgreSQL está rodando
docker ps

# 2. Reiniciar o banco
docker-compose down
docker-compose up -d

# 3. Limpar cache do Maven
mvn clean

# 4. Verificar a API Key do OpenAI
echo %QUARKUS_LANGCHAIN4J_OPENAI_API_KEY%
```

### Problema: Erro de conexão com banco de dados

**Sintomas:** `Connection refused` ou `Cannot connect to database`

**Soluções:**
```bash
# Verificar se o PostgreSQL está rodando
docker-compose ps

# Ver logs do PostgreSQL
docker-compose logs postgres

# Reiniciar o container
docker-compose restart postgres

# Verificar porta 5432
netstat -an | findstr 5432
```

### Problema: Upload de documento falha

**Sintomas:** Erro 500 ao fazer upload

**Soluções:**
- Verificar se o arquivo existe no caminho especificado
- Verificar tamanho do arquivo (limite padrão: 10MB)
- Verificar formato do arquivo (TXT, PDF suportados)
- Ver logs da aplicação para detalhes do erro

### Problema: Respostas vazias ou incorretas

**Sintomas:** A IA retorna respostas genéricas ou vazias

**Soluções:**
1. Verificar se documentos foram carregados corretamente
2. Verificar se a API Key do OpenAI é válida
3. Aumentar o `maxResults` na requisição
4. Verificar logs para erros de embeddings

### Problema: Erro de API Key

**Sintomas:** `Unauthorized` ou `Invalid API Key`

**Soluções:**
```bash
# Windows CMD
set QUARKUS_LANGCHAIN4J_OPENAI_API_KEY=sk-sua-chave-aqui

# PowerShell
$env:QUARKUS_LANGCHAIN4J_OPENAI_API_KEY="sk-sua-chave-aqui"

# Ou editar application.properties
# quarkus.langchain4j.openai.api-key=sk-sua-chave-aqui
```

### Problema: Porta 8080 já em uso

**Sintomas:** `Port 8080 already in use`

**Soluções:**
```bash
# Mudar porta no application.properties
# quarkus.http.port=8081

# Ou via variável de ambiente
set QUARKUS_HTTP_PORT=8081
```

---

## 📊 Monitoramento e Logs

### Ver Logs da Aplicação
```bash
# Durante execução em dev mode, os logs aparecem no console

# Para produção, redirecionar para arquivo
java -jar target/quarkus-app/quarkus-run.jar > app.log 2>&1
```

### Health Check
```bash
# Verificar saúde da aplicação
curl http://localhost:8080/q/health

# Verificar métricas
curl http://localhost:8080/q/metrics
```

### Swagger UI (se habilitado)
```
http://localhost:8080/q/swagger-ui
```

---

## 🎓 Conceitos Importantes

### RAG (Retrieval-Augmented Generation)
Técnica que combina:
1. **Retrieval:** Busca semântica em documentos usando embeddings
2. **Augmentation:** Contextualiza a pergunta com documentos relevantes
3. **Generation:** LLM gera resposta baseada no contexto recuperado

### Embeddings
Representação vetorial de texto que captura significado semântico.
- Dimensão: 1536 (OpenAI text-embedding-ada-002)
- Armazenamento: PostgreSQL + pgvector
- Busca: Similaridade por cosseno

### Multi-Agentes
Sistema com múltiplos agentes especializados trabalhando em colaboração:
- Cada agente tem função específica
- Pipeline orquestrado para melhor resultado
- Transparência nas etapas de processamento

---

## 📚 Referências e Documentação

### Tecnologias Utilizadas
- **Quarkus:** https://quarkus.io/
- **LangChain4j:** https://docs.langchain4j.dev/
- **PostgreSQL:** https://www.postgresql.org/
- **pgvector:** https://github.com/pgvector/pgvector
- **OpenAI:** https://platform.openai.com/docs

### Documentação Adicional
- Quarkus LangChain4j: https://docs.quarkiverse.io/quarkus-langchain4j/
- gRPC: https://grpc.io/
- Protocol Buffers: https://protobuf.dev/

---

## 🎉 Conclusão

Você agora tem um sistema RAG completo com multi-agentes funcionando! 

### Próximos Passos
1. ✅ Carregar seus próprios documentos
2. ✅ Testar diferentes tipos de perguntas
3. ✅ Comparar as abordagens LangChain4j vs MCP
4. ✅ Explorar métricas de performance
5. ✅ Customizar os agentes conforme necessidade

### Suporte
Para dúvidas ou problemas:
- Verificar seção de Troubleshooting
- Consultar logs da aplicação
- Revisar configurações do application.properties

---

**Versão do Guia:** 1.0  
**Data:** Outubro 2025  
**Projeto:** QUARKUS-MCP RAG Multi-Agentes

