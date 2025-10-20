package com.quarkus.rag.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.SessionScoped;

@RegisterAiService
@SessionScoped
public interface ChatService {

    @SystemMessage("""
        Você é um assistente útil que responde perguntas baseado no contexto fornecido.
        Use apenas as informações do contexto para responder.
        Se a resposta não estiver no contexto, diga que não sabe.
        """)
    @UserMessage("""
        Contexto:
        {context}
        
        Pergunta: {question}
        """)
    String chat(String context, String question);
}

