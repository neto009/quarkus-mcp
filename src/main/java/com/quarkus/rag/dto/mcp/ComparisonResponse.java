package com.quarkus.rag.dto.mcp;

public record ComparisonResponse(
    ApproachResult langchainResult,
    ApproachResult mcpResult,
    ComparisonMetrics metrics
) {
}

