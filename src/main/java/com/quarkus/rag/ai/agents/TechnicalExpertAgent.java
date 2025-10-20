package com.quarkus.rag.ai.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

/**
 * Agente especializado em responder perguntas técnicas
 */
@RegisterAiService
public interface TechnicalExpertAgent {

    @SystemMessage("""
        Você é um especialista técnico altamente qualificado.
        Sua função é:
        - Responder perguntas técnicas com precisão
        - Fornecer explicações detalhadas quando necessário
        - Usar terminologia técnica apropriada
        - Citar fontes quando disponível no contexto
        """)
    @UserMessage("""
        Com base no contexto fornecido, responda a pergunta técnica:

        Contexto: {context}

        Pergunta: {question}
        """)
    String answerTechnicalQuestion(String context, String question);
}

