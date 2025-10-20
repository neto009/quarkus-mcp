package com.quarkus.rag.resource;

import com.quarkus.rag.service.MultiAgentOrchestrator;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/api/agents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgentResource {

    @Inject
    MultiAgentOrchestrator orchestrator;

    /**
     * Endpoint para executar o pipeline completo de multi-agentes
     * Retorna todos os passos intermediários para transparência
     */
    @POST
    @Path("/ask")
    public MultiAgentOrchestrator.AgentResponse askWithAgents(AgentRequest request) {
        return orchestrator.executeAgentPipeline(
            request.question(),
            request.maxResults() != null ? request.maxResults() : 5
        );
    }

    /**
     * Endpoint simplificado - apenas a resposta final
     */
    @POST
    @Path("/ask-simple")
    public SimpleResponse askSimple(AgentRequest request) {
        String answer = orchestrator.executeSimplified(
            request.question(),
            request.maxResults() != null ? request.maxResults() : 5
        );
        return new SimpleResponse(answer);
    }

    /**
     * Analisa um documento usando múltiplos agentes
     */
    @POST
    @Path("/analyze")
    public MultiAgentOrchestrator.DocumentAnalysisResult analyzeDocument(AnalyzeRequest request) {
        return orchestrator.analyzeWithAgents(request.content());
    }

    public record AgentRequest(String question, Integer maxResults) {}
    public record SimpleResponse(String answer) {}
    public record AnalyzeRequest(String content) {}
}

