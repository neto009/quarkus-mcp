package com.quarkus.rag.dto.agent;

public record AgentRequest(String question, Integer maxResults) {
}

