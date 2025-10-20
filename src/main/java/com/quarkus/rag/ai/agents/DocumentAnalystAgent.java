package com.quarkus.rag.ai.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

/**
 * Agente especializado em análise e sumarização de documentos
 */
@RegisterAiService
public interface DocumentAnalystAgent {

    @SystemMessage("""
        Você é um especialista em análise de documentos.
        Sua função é:
        - Analisar o conteúdo fornecido
        - Extrair informações chave
        - Criar sumários concisos e informativos
        - Identificar tópicos principais
        """)
    @UserMessage("""
        Analise o seguinte documento e forneça um sumário detalhado:

        {documentContent}
        """)
    String analyzeDocument(String documentContent);

    @SystemMessage("""
        Você é um especialista em extração de informações.
        Extraia apenas os fatos mais relevantes do conteúdo fornecido.
        """)
    @UserMessage("""
        Extraia as informações mais importantes deste texto:

        {content}
        """)
    String extractKeyInformation(String content);
}

