# Pré-processamento de Texto para Embeddings

## 📋 Visão Geral

Foi implementado um sistema robusto de pré-processamento de texto usando **Apache Lucene 9.9.2**, uma das bibliotecas mais utilizadas no mercado para análise e processamento de texto.

## 🚀 Funcionalidades Implementadas

### 1. **TextPreprocessingService**
Serviço centralizado que aplica todas as transformações necessárias para melhorar a qualidade dos embeddings.

#### Transformações Aplicadas:

1. **Garantia de UTF-8**
   - Converte e valida que todo texto está em UTF-8
   - Previne problemas de encoding

2. **Remoção de HTML Tags**
   - Remove tags HTML que podem estar presentes nos documentos
   - Limpa o texto mantendo apenas conteúdo relevante

3. **Normalização de Caracteres**
   - Normaliza caracteres Unicode (forma NFC)
   - Mantém acentos (importante para português)
   - Remove caracteres especiais problemáticos

4. **Normalização de Espaços**
   - Remove múltiplos espaços consecutivos
   - Normaliza tabs e quebras de linha
   - Limpa espaços no início e fim

5. **Análise Lucene para Português** (BrazilianAnalyzer)
   - **Lowercase**: Converte todo texto para minúsculas
   - **Remoção de Stopwords**: Remove palavras comuns em português (de, da, o, a, para, etc.)
   - **Stemming**: Reduz palavras à raiz (correndo → corr, corrida → corr)
   - **Tokenização**: Quebra o texto em tokens relevantes
   - **Filtro de tamanho mínimo**: Remove tokens muito pequenos (< 2 caracteres)

## 🔧 Integração no Sistema

### 1. DocumentIngestionService
```java
// Pré-processa documentos antes de criar embeddings
String preprocessedText = textPreprocessingService.preprocessForEmbedding(originalText);
```

**Fluxo:**
1. Parser lê o documento (PDF, DOCX, TXT)
2. Texto é pré-processado e limpo
3. Validação de qualidade do texto
4. Documento processado é dividido em segmentos
5. Embeddings são criados com texto de qualidade

### 2. RagService
```java
// Pré-processa a pergunta do usuário
String processedQuestion = textPreprocessingService.preprocessForQuery(question);
```

**Fluxo:**
1. Pergunta do usuário é pré-processada
2. Busca no banco vetorial usa texto processado
3. Melhora a similaridade entre pergunta e documentos
4. Resposta usa pergunta original (melhor contexto para LLM)

## 📦 Dependências Adicionadas

```xml
<!-- Apache Lucene for Text Processing -->
<dependency>
    <groupId>org.apache.lucene</groupId>
    <artifactId>lucene-core</artifactId>
    <version>9.9.2</version>
</dependency>
<dependency>
    <groupId>org.apache.lucene</groupId>
    <artifactId>lucene-analysis-common</artifactId>
    <version>9.9.2</version>
</dependency>
```

## 🎯 Benefícios

### Qualidade dos Embeddings
- ✅ Texto limpo e normalizado
- ✅ Remoção de ruído (stopwords, caracteres especiais)
- ✅ Consistência no processamento
- ✅ Melhor similaridade semântica

### Busca Mais Precisa
- ✅ Pergunta e documentos processados da mesma forma
- ✅ Menos falsos negativos
- ✅ Resultados mais relevantes

### Produção Ready
- ✅ Biblioteca enterprise-grade (Apache Lucene)
- ✅ Otimizada para português (BrazilianAnalyzer)
- ✅ Tratamento robusto de erros
- ✅ Logs detalhados

## 🔍 Métodos Disponíveis

### `preprocessForEmbedding(String text)`
Aplica todas as transformações para preparar texto para embeddings.
Usado em: Ingestão de documentos

### `preprocessForQuery(String query)`
Versão otimizada para queries (mantém mais contexto).
Usado em: Perguntas dos usuários

### `isValidForEmbedding(String text)`
Valida se o texto tem qualidade suficiente (mínimo 10 caracteres após processamento).

## 📊 Exemplo de Transformação

**Texto Original:**
```
   A Catedral da TI oferece SOLUÇÕES inovadoras!!!   
   Para empresas de todos os portes...
```

**Após Pré-processamento:**
```
catedral ti oferec soluc inov empres port
```

**Benefícios:**
- Remoção de espaços extras
- Lowercase aplicado
- Stopwords removidas (a, da, de, os, para)
- Stemming aplicado (oferece → oferec, soluções → soluc, etc.)
- Pontuação desnecessária removida

## ⚙️ Configuração

O serviço funciona automaticamente, sem necessidade de configuração adicional.
Está integrado via CDI (Jakarta Inject) e é singleton por padrão.

## 🧪 Validação

O sistema valida:
- ✅ Texto não nulo/vazio
- ✅ Conversão UTF-8 bem-sucedida
- ✅ Tamanho mínimo após processamento
- ✅ Tokens válidos extraídos

## 🚨 Tratamento de Erros

Em caso de erro no processamento:
- Logs detalhados são gerados
- Fallback para processamento básico (normalização de espaços)
- Exceções são capturadas e tratadas
- Sistema não interrompe funcionamento

## 📈 Performance

- Processamento eficiente com Lucene
- Cache de analyzer (reutilizado)
- Mínimo overhead no processo de ingestão
- Otimizado para grandes volumes de texto

## 🔄 Próximos Passos (Opcional)

- [ ] Adicionar configuração para habilitar/desabilitar stemming
- [ ] Suporte para múltiplos idiomas
- [ ] Métricas de qualidade do texto
- [ ] Cache de textos pré-processados

