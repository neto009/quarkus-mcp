package com.quarkus.rag.service;

import com.quarkus.rag.ai.ChatService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class RagService {

    @Inject
    RetrievalService retrievalService;

    @Inject
    ChatService chatService;

    @Inject
    TextPreprocessingService textPreprocessingService;

    public String ask(String question, int maxResults) {
        // 1. Pré-processar a pergunta do usuário
        String processedQuestion = textPreprocessingService.preprocessForQuery(question);

        // 2. Retrieve relevant documents usando a pergunta processada
        List<String> relevantDocs = retrievalService.retrieve(processedQuestion, maxResults);

        if (relevantDocs.isEmpty()) {
            return "Desculpe, não encontrei informações relevantes para responder sua pergunta.";
        }

        // 3. Build context from retrieved documents
        String context = String.join("\n\n", relevantDocs);

        // 4. Generate answer using LLM with context (usa pergunta original para melhor resposta)
        return chatService.chat(context, question);
    }
}

