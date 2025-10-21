package com.quarkus.rag.dto.chat;

public record ChatRequest(String question, Integer maxResults) {
}

