package com.quarkus.rag.dto.mcp;

public record ApproachResult(
    String approachName,
    String finalAnswer,
    String documentAnalysis,
    String technicalAnswer,
    String validation,
    long executionTimeMs,
    String error
) {
}

