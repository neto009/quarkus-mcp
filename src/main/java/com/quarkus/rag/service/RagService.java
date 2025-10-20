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

    public String ask(String question, int maxResults) {
        // 1. Retrieve relevant documents
        List<String> relevantDocs = retrievalService.retrieve(question, maxResults);

        if (relevantDocs.isEmpty()) {
            return "Desculpe, não encontrei informações relevantes para responder sua pergunta.";
        }

        // 2. Build context from retrieved documents
        String context = String.join("\n\n", relevantDocs);

        // 3. Generate answer using LLM with context
        return chatService.chat(context, question);
    }
}

