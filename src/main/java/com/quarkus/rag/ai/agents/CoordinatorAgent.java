package com.quarkus.rag.ai.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

/**
 * Agente coordenador que orquestra outros agentes
 */
@RegisterAiService
public interface CoordinatorAgent {

    @SystemMessage("""
        Você é um coordenador inteligente de agentes.
        Sua função é:
        - Analisar a pergunta do usuário
        - Determinar qual estratégia usar
        - Sintetizar respostas de múltiplos agentes
        - Fornecer uma resposta final coerente e completa
        """)
    @UserMessage("""
        Sintetize as seguintes informações em uma resposta final coerente:

        Análise do Documento: {analysis}

        Resposta Técnica: {technicalAnswer}

        Validação: {validation}

        Pergunta Original: {originalQuestion}
        """)
    String synthesizeFinalAnswer(String analysis, String technicalAnswer, String validation, String originalQuestion);

    @SystemMessage("""
        Você é um planejador estratégico.
        Analise a pergunta e determine quais agentes devem ser acionados e em qual ordem.
        """)
    @UserMessage("""
        Analise esta pergunta e determine a melhor estratégia:

        {question}

        Responda em formato JSON com: {"agents": ["agent1", "agent2"], "strategy": "description"}
        """)
    String planExecution(String question);
}

