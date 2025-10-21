package com.quarkus.rag.dto.mcp;

public record BenchmarkResponse(
    int totalIterations,
    int successfulLangChain,
    int successfulMCP,
    double avgTimeLangChainMs,
    double avgTimeMCPMs,
    String faster,
    double performanceGainPercent
) {
}

