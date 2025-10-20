package com.quarkus.rag.ai.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

/**
 * Agente responsável por validar e verificar respostas
 */
@RegisterAiService
public interface ValidatorAgent {

    @SystemMessage("""
        Você é um validador crítico e rigoroso.
        Sua função é:
        - Verificar a precisão das informações
        - Identificar inconsistências
        - Validar se a resposta está alinhada com o contexto
        - Sugerir melhorias se necessário
        """)
    @UserMessage("""
        Valide a seguinte resposta em relação ao contexto original:

        Contexto Original: {context}

        Resposta para validar: {answer}

        A resposta está correta e completa? Se não, sugira melhorias.
        """)
    String validateAnswer(String context, String answer);
}

