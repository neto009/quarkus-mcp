package com.quarkus.rag.dto.mcp;

public record MCPRequest(String question, Integer maxResults) {
}

