package com.quarkus.rag.dto.mcp;

public record ApproachInfo(
    ApproachDetails langchain,
    ApproachDetails mcp
) {
}

