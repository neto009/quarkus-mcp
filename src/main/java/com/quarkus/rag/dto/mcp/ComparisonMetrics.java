package com.quarkus.rag.dto.mcp;

public record ComparisonMetrics(
    long langchainTimeMs,
    long mcpTimeMs,
    String faster,
    double performanceGainPercent
) {
}

