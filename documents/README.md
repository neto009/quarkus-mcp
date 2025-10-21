# Documents Directory

Este diretório é usado pelo Easy RAG para carregar documentos automaticamente na inicialização.

## Como usar:

1. Coloque seus documentos (PDF, Word, TXT) neste diretório
2. O Quarkus vai processar e indexar automaticamente na inicialização
3. Os documentos estarão disponíveis para busca via RAG

## Tipos de arquivo suportados:

- PDF (.pdf)
- Word (.docx, .doc)
- Text (.txt)
- E muitos outros via Apache Tika

## Nota:

Se você não quiser usar o Easy RAG automático, pode desabilitar nas configurações:
```
quarkus.langchain4j.easy-rag.enabled=false
```

Ou simplesmente mantenha este diretório vazio e faça upload via API.

