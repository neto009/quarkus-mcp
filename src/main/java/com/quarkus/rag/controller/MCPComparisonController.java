package com.quarkus.rag.controller;

import com.quarkus.rag.dto.mcp.*;
import com.quarkus.rag.mcp.service.MCPOrchestrator;
import com.quarkus.rag.service.MultiAgentOrchestrator;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

/**
 * Endpoint de comparação entre abordagens de Multi-Agentes:
 * 1. LangChain4j puro (implementação manual)
 * 2. MCP + gRPC (protocolo estruturado)
 */
@Path("/api/mcp")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MCPComparisonController {

    private static final Logger LOG = Logger.getLogger(MCPComparisonController.class);

    @Inject
    MultiAgentOrchestrator langchainOrchestrator;

    @Inject
    MCPOrchestrator mcpOrchestrator;

    /**
     * Endpoint usando MCP + gRPC para comunicação entre agentes
     */
    @POST
    @Path("/ask")
    public MCPOrchestrator.MCPAgentResponse askWithMCP(MCPRequest request) {
        LOG.info("[MCP Endpoint] Processing question with MCP+gRPC protocol");
        return mcpOrchestrator.executeWithMCP(
            request.question(),
            request.maxResults() != null ? request.maxResults() : 5
        );
    }

    /**
     * Endpoint de comparação lado a lado
     */
    @POST
    @Path("/compare")
    public ComparisonResponse compareApproaches(MCPRequest request) {
        LOG.info("[Comparison] Executing both approaches for comparison");

        long startLangChain = System.currentTimeMillis();
        MultiAgentOrchestrator.AgentResponse langchainResponse = null;
        Exception langchainError = null;

        try {
            langchainResponse = langchainOrchestrator.executeAgentPipeline(
                request.question(),
                request.maxResults() != null ? request.maxResults() : 5
            );
        } catch (Exception e) {
            LOG.error("[Comparison] LangChain approach failed", e);
            langchainError = e;
        }
        long langchainDuration = System.currentTimeMillis() - startLangChain;

        long startMCP = System.currentTimeMillis();
        MCPOrchestrator.MCPAgentResponse mcpResponse = null;
        Exception mcpError = null;

        try {
            mcpResponse = mcpOrchestrator.executeWithMCP(
                request.question(),
                request.maxResults() != null ? request.maxResults() : 5
            );
        } catch (Exception e) {
            LOG.error("[Comparison] MCP approach failed", e);
            mcpError = e;
        }
        long mcpDuration = System.currentTimeMillis() - startMCP;

        return new ComparisonResponse(
            new ApproachResult(
                "LangChain4j Pure",
                langchainResponse != null ? langchainResponse.finalAnswer() : null,
                langchainResponse != null ? langchainResponse.documentAnalysis() : null,
                langchainResponse != null ? langchainResponse.technicalAnswer() : null,
                langchainResponse != null ? langchainResponse.validation() : null,
                langchainDuration,
                langchainError != null ? langchainError.getMessage() : null
            ),
            new ApproachResult(
                "MCP + gRPC",
                mcpResponse != null ? mcpResponse.finalAnswer() : null,
                mcpResponse != null ? mcpResponse.documentAnalysis() : null,
                mcpResponse != null ? mcpResponse.technicalAnswer() : null,
                mcpResponse != null ? mcpResponse.validation() : null,
                mcpDuration,
                mcpError != null ? mcpError.getMessage() : null
            ),
            new ComparisonMetrics(
                langchainDuration,
                mcpDuration,
                mcpDuration < langchainDuration ? "MCP+gRPC" : "LangChain4j",
                calculatePerformanceGain(langchainDuration, mcpDuration)
            )
        );
    }

    /**
     * Executa múltiplas vezes para benchmark
     */
    @POST
    @Path("/benchmark")
    public BenchmarkResponse benchmark(BenchmarkRequest request) {
        LOG.info("[Benchmark] Running " + request.iterations() + " iterations");

        long totalLangChain = 0;
        long totalMCP = 0;
        int successLangChain = 0;
        int successMCP = 0;

        for (int i = 0; i < request.iterations(); i++) {
            LOG.info("[Benchmark] Iteration " + (i + 1) + "/" + request.iterations());

            // LangChain approach
            long startLangChain = System.currentTimeMillis();
            try {
                langchainOrchestrator.executeAgentPipeline(request.question(), 5);
                totalLangChain += System.currentTimeMillis() - startLangChain;
                successLangChain++;
            } catch (Exception e) {
                LOG.error("[Benchmark] LangChain iteration failed", e);
            }

            // MCP approach
            long startMCP = System.currentTimeMillis();
            try {
                mcpOrchestrator.executeWithMCP(request.question(), 5);
                totalMCP += System.currentTimeMillis() - startMCP;
                successMCP++;
            } catch (Exception e) {
                LOG.error("[Benchmark] MCP iteration failed", e);
            }
        }

        double avgLangChain = successLangChain > 0 ? (double) totalLangChain / successLangChain : 0;
        double avgMCP = successMCP > 0 ? (double) totalMCP / successMCP : 0;

        return new BenchmarkResponse(
            request.iterations(),
            successLangChain,
            successMCP,
            avgLangChain,
            avgMCP,
            avgMCP < avgLangChain ? "MCP+gRPC" : "LangChain4j",
            calculatePerformanceGain((long) avgLangChain, (long) avgMCP)
        );
    }

    /**
     * Retorna informações sobre as diferenças entre as abordagens
     */
    @GET
    @Path("/info")
    public ApproachInfo getApproachInfo() {
        return new ApproachInfo(
            new ApproachDetails(
                "LangChain4j Pure",
                "Implementação manual usando LangChain4j",
                new String[]{
                    "Comunicação direta entre agentes via interfaces Java",
                    "Sem overhead de serialização",
                    "Mais simples de implementar e debugar",
                    "Integração nativa com ecossistema LangChain4j"
                },
                new String[]{
                    "Menos estruturado",
                    "Difícil rastreamento de chamadas entre agentes",
                    "Sem suporte nativo a streaming",
                    "Acoplamento mais forte entre componentes"
                }
            ),
            new ApproachDetails(
                "MCP + gRPC",
                "Model Context Protocol com gRPC para comunicação estruturada",
                new String[]{
                    "Protocolo bem definido via Protocol Buffers",
                    "Comunicação tipada e estruturada",
                    "Rastreabilidade completa (session_id, timestamps, metadata)",
                    "Suporte a streaming bidirecional",
                    "Serialização binária eficiente",
                    "Desacoplamento total entre agentes",
                    "Possibilidade de distribuição em múltiplos serviços"
                },
                new String[]{
                    "Mais complexo de implementar",
                    "Overhead de serialização/deserialização",
                    "Requer definição de schemas .proto",
                    "Curva de aprendizado maior"
                }
            )
        );
    }

    private double calculatePerformanceGain(long time1, long time2) {
        if (time1 == 0) return 0;
        return ((double) (time1 - time2) / time1) * 100;
    }
}

