package com.quarkus.rag.dto.mcp;

public record ApproachDetails(
    String name,
    String description,
    String[] advantages,
    String[] disadvantages
) {
}

