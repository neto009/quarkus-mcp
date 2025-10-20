# Quarkus RAG Multi-Agent System

Sistema completo de RAG (Retrieval-Augmented Generation) com Multi-Agentes usando Quarkus, PostgreSQL/PGVector e OpenAI.

## 🚀 Tecnologias

- **Quarkus 3.15.1** - Framework Java reativo
- **Quarkus LangChain4j** - Integração com LLMs (similar ao Spring AI)
- **PostgreSQL + PGVector** - Banco vetorial para embeddings
- **OpenAI API** - GPT-4 e text-embedding-3-small
- **Multi-Agentes A2A** - 4 agentes especializados colaborando

## 📋 Instalação e Execução

### 1. Pré-requisitos
- Java 17+
- Maven 3.8+
- Docker
- OpenAI API Key

### 2. Iniciar PostgreSQL com PGVector

```bash
docker-compose up -d
```

### 3. Configurar OpenAI API Key

**Windows CMD:**
```cmd
set OPENAI_API_KEY=sk-sua-chave-aqui
```

**Windows PowerShell:**
```powershell
$env:OPENAI_API_KEY="sk-sua-chave-aqui"
```

**Ou edite diretamente:** `src/main/resources/application.properties`
```properties
quarkus.langchain4j.openai.api-key=sk-sua-chave-real
```

### 4. Executar

```bash
mvnw quarkus:dev
```

Acesse: http://localhost:8080

## 📚 Arquitetura

### Sistema RAG
1. **Upload** → Documentos são divididos em chunks
2. **Embedding** → Chunks convertidos em vetores (OpenAI)
3. **Storage** → Vetores salvos no PGVector
4. **Retrieval** → Busca semântica por chunks relevantes
5. **Generation** → LLM gera resposta com contexto

### Multi-Agentes A2A (Agent-to-Agent)

**Pipeline de Colaboração:**
```
Pergunta
  ↓
1. RetrievalService → Busca documentos relevantes
  ↓
2. DocumentAnalystAgent → Analisa e sumariza contexto
  ↓
3. TechnicalExpertAgent → Responde a pergunta
  ↓
4. ValidatorAgent → Valida a resposta
  ↓
5. CoordinatorAgent → Sintetiza resposta final
  ↓
Resposta completa + passos intermediários
```

**Agentes Disponíveis:**
- **DocumentAnalystAgent** - Especialista em análise de documentos
- **TechnicalExpertAgent** - Responde perguntas técnicas
- **ValidatorAgent** - Valida precisão das respostas
- **CoordinatorAgent** - Coordena e sintetiza tudo

## 🔌 API Endpoints

### 1. Upload de Documento

**Endpoint:** `POST /api/documents/upload`

```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@documento.pdf"
```

**Suporta:** PDF, Word (.docx), Text (.txt)

---

### 2. Chat RAG Simples

**Endpoint:** `POST /api/chat`

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d "{\"question\":\"Qual é o tema principal do documento?\"}"
```

**Com mais resultados:**
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d "{\"question\":\"Resuma os pontos principais\",\"maxResults\":10}"
```

**Resposta:**
```json
{
  "answer": "O tema principal é..."
}
```

---

### 3. Multi-Agentes - Pipeline Completo

**Endpoint:** `POST /api/agents/ask`

```bash
curl -X POST http://localhost:8080/api/agents/ask \
  -H "Content-Type: application/json" \
  -d "{\"question\":\"Explique os conceitos técnicos principais\"}"
```

**Resposta com todos os passos:**
```json
{
  "finalAnswer": "Resposta sintetizada final...",
  "documentAnalysis": "O DocumentAnalyst identificou...",
  "technicalAnswer": "O TechnicalExpert explica que...",
  "validation": "O Validator confirmou que...",
  "retrievedContext": "Contexto recuperado do banco..."
}
```

**Exemplo complexo:**
```bash
curl -X POST http://localhost:8080/api/agents/ask \
  -H "Content-Type: application/json" \
  -d "{\"question\":\"Quais são os riscos e como mitigá-los?\",\"maxResults\":8}"
```

---

### 4. Multi-Agentes - Simplificado

**Endpoint:** `POST /api/agents/ask-simple`

```bash
curl -X POST http://localhost:8080/api/agents/ask-simple \
  -H "Content-Type: application/json" \
  -d "{\"question\":\"Qual é o tema principal?\"}"
```

**Resposta:**
```json
{
  "answer": "Apenas a resposta final..."
}
```

---

### 5. Análise de Documento por Agentes

**Endpoint:** `POST /api/agents/analyze`

```bash
curl -X POST http://localhost:8080/api/agents/analyze \
  -H "Content-Type: application/json" \
  -d "{\"content\":\"Seu texto longo para análise...\"}"
```

**Resposta:**
```json
{
  "summary": "Sumário completo...",
  "keyInformation": "Informações chave extraídas..."
}
```

---

### 6. Listar Documentos

**Endpoint:** `GET /api/documents`

```bash
curl http://localhost:8080/api/documents
```

---

### 7. Obter Documento Específico

**Endpoint:** `GET /api/documents/{id}`

```bash
curl http://localhost:8080/api/documents/1
```

---

### 8. Deletar Documento

**Endpoint:** `DELETE /api/documents/{id}`

```bash
curl -X DELETE http://localhost:8080/api/documents/1
```

---

## 🎯 Exemplos de Uso

### Fluxo Completo

```bash
# 1. Upload documento
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@manual_tecnico.pdf"

# 2. Chat simples
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d "{\"question\":\"Como instalar o sistema?\"}"

# 3. Análise profunda com Multi-Agentes
curl -X POST http://localhost:8080/api/agents/ask \
  -H "Content-Type: application/json" \
  -d "{\"question\":\"Detalhe os requisitos de segurança e implementação\"}"
```

### Quando Usar Cada Endpoint

| Endpoint | Quando Usar | Velocidade | Precisão |
|----------|-------------|------------|----------|
| `/api/chat` | Perguntas simples | ⚡ Rápido | ✓ Boa |
| `/api/agents/ask-simple` | Perguntas técnicas | ⚡ Médio | ✓✓ Muito Boa |
| `/api/agents/ask` | Análise complexa + transparência | ⚡ Lento | ✓✓✓ Excelente |

## 🧠 Como Funciona

### RAG (Retrieval-Augmented Generation)

**Problema:** LLMs não conhecem seus documentos específicos

**Solução:** RAG adiciona seus documentos como contexto

```
Sem RAG: Pergunta → GPT → Resposta genérica (pode alucinar)
Com RAG: Pergunta → Busca em docs → GPT + Contexto → Resposta precisa
```

### Embeddings

Transformam texto em vetores matemáticos:
```
"cachorro" → [0.2, 0.8, 0.3, ..., 0.5]  (1536 números)
"gato"     → [0.3, 0.7, 0.4, ..., 0.4]
```

Palavras similares têm vetores próximos, permitindo busca semântica.

### Multi-Agentes A2A

**Vantagens:**
- ✅ Cada agente é especialista em sua área
- ✅ Validação cruzada melhora qualidade
- ✅ Transparência: vê o raciocínio de cada agente
- ✅ Flexível: fácil adicionar novos agentes

**Desvantagens:**
- ❌ Mais lento (múltiplas chamadas à OpenAI)
- ❌ Maior custo de API

## ⚙️ Configurações

### application.properties

```properties
# OpenAI
quarkus.langchain4j.openai.api-key=sk-sua-chave
quarkus.langchain4j.openai.chat-model.model-name=gpt-4o-mini
quarkus.langchain4j.openai.embedding-model.model-name=text-embedding-3-small

# PGVector
quarkus.langchain4j.pgvector.dimension=1536

# Easy RAG (carrega docs da pasta ./documents automaticamente)
quarkus.langchain4j.easy-rag.path=./documents
quarkus.langchain4j.easy-rag.max-segment-size=1000
quarkus.langchain4j.easy-rag.max-overlap-size=200
```

### Desabilitar Easy RAG

Se não quiser carregar documentos automaticamente da pasta `./documents`:

```properties
quarkus.langchain4j.easy-rag.enabled=false
```

## 🔧 Customização

### Adicionar Novo Agente

1. Crie a interface:

```java
@RegisterAiService
public interface MyCustomAgent {
    @SystemMessage("Você é um especialista em...")
    @UserMessage("{input}")
    String process(String input);
}
```

2. Injete no `MultiAgentOrchestrator`:

```java
@Inject
MyCustomAgent myAgent;
```

3. Use no pipeline:

```java
String result = myAgent.process(context);
```

## 🐛 Troubleshooting

### Erro: "Could not expand value OPENAI_API_KEY"
Configure a variável de ambiente ou edite `application.properties`

### Erro: "Table embeddings does not exist"
```bash
docker exec -it postgres-pgvector psql -U postgres -d ragdb \
  -c "CREATE EXTENSION IF NOT EXISTS vector;"
```

### PostgreSQL não conecta
```bash
docker ps | findstr postgres
docker start postgres-pgvector
```

### Porta 8080 em uso
Altere em `application.properties`:
```properties
quarkus.http.port=8081
```

## 📦 Estrutura do Projeto

```
src/main/java/com/quarkus/rag/
├── ai/
│   ├── ChatService.java              # Chat RAG básico
│   └── agents/                        # 4 agentes especializados
│       ├── CoordinatorAgent.java
│       ├── DocumentAnalystAgent.java
│       ├── TechnicalExpertAgent.java
│       └── ValidatorAgent.java
├── entity/
│   └── Document.java                  # JPA Entity
├── repository/
│   └── DocumentRepository.java        # Panache Repository
├── resource/                          # REST APIs
│   ├── AgentResource.java
│   ├── ChatResource.java
│   └── DocumentResource.java
└── service/
    ├── DocumentIngestionService.java  # Processa uploads
    ├── MultiAgentOrchestrator.java    # Orquestra A2A
    ├── RagService.java                # RAG básico
    └── RetrievalService.java          # Busca vetorial
```

## 🌐 URLs Úteis

- **Aplicação:** http://localhost:8080
- **Dev UI:** http://localhost:8080/q/dev
- **Health Check:** http://localhost:8080/q/health
- **Métricas:** http://localhost:8080/q/metrics

## 📝 Notas Importantes

### Easy RAG Automático
Coloque documentos em `./documents/` e eles serão processados automaticamente na inicialização.

### Abstrações Quarkus LangChain4j
Você não precisa escrever código boilerplate:

```java
// Quarkus gera implementação automaticamente
@RegisterAiService
public interface ChatService {
    @SystemMessage("Você é...")
    @UserMessage("{question}")
    String chat(String question);
}
```

### Performance
- **Chunks pequenos (500):** Perdem contexto
- **Chunks grandes (5000):** Busca imprecisa
- **Ideal (1000):** Balance perfeito ✅

### Custos OpenAI
- Embeddings: ~$0.0001 por 1K tokens
- GPT-4o-mini: ~$0.15 por 1M tokens input
- Multi-agentes: 4-5x mais chamadas = maior custo

## 📄 Licença

MIT License

---

**Criado com ❤️ usando Quarkus + LangChain4j + PGVector**
