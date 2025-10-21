package com.quarkus.rag.mcp.service;

import com.quarkus.rag.mcp.proto.*;
import com.quarkus.rag.service.RetrievalService;
import io.grpc.ManagedChannel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Orchestrador de Multi-Agentes usando MCP (Model Context Protocol) com gRPC
 *
 * Esta implementação demonstra o uso de protocolos estruturados para comunicação
 * entre agentes, permitindo comparação direta com a abordagem LangChain4j pura.
 *
 * Características MCP + gRPC:
 * - Comunicação tipada e estruturada via Protocol Buffers
 * - Suporte a streaming bidirecional
 * - Contexto compartilhado entre agentes (session_id, metadata)
 * - Rastreabilidade completa de cada chamada
 * - Performance otimizada com serialização binária
 */
@ApplicationScoped
public class MCPOrchestrator {

    private static final Logger LOG = Logger.getLogger(MCPOrchestrator.class);
    private static final String GRPC_HOST = "localhost";
    private static final int GRPC_PORT = 9090;

    @Inject
    RetrievalService retrievalService;

    @Inject
    MCPAgentService mcpAgentService;

    private ManagedChannel channel;
    private AgentCommunicationServiceGrpc.AgentCommunicationServiceBlockingStub blockingStub;

    @PostConstruct
    public void init() {
        LOG.info("[MCP Orchestrator] Initializing gRPC channel");
        // Para uso interno (in-process), usamos o serviço diretamente
        // Em produção, isso seria uma conexão real gRPC
    }

    @PreDestroy
    public void cleanup() {
        if (channel != null && !channel.isShutdown()) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                LOG.info("[MCP Orchestrator] gRPC channel closed");
            } catch (InterruptedException e) {
                LOG.error("[MCP Orchestrator] Error closing channel", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Executa o pipeline completo usando MCP + gRPC
     * Esta é a versão comparável ao MultiAgentOrchestrator
     */
    public MCPAgentResponse executeWithMCP(String question, int maxResults) {
        String sessionId = UUID.randomUUID().toString();
        LOG.info("[MCP Pipeline] Starting session: " + sessionId);

        long startTime = System.currentTimeMillis();

        try {
            // Step 1: Retrieve relevant documents (mesmo processo)
            LOG.info("[MCP Pipeline] Step 1: Retrieving documents...");
            List<String> relevantDocs = retrievalService.retrieve(question, maxResults);

            if (relevantDocs.isEmpty()) {
                return new MCPAgentResponse(
                    "Não encontrei documentos relevantes para responder sua pergunta.",
                    null, null, null, null, sessionId, "MCP+gRPC", 0
                );
            }

            String context = String.join("\n\n", relevantDocs);

            // Step 2: Document Analysis via MCP/gRPC
            LOG.info("[MCP Pipeline] Step 2: Document Analyst (via MCP)...");
            AnalyzeResponse analysisResponse = callAnalyzeDocument(sessionId, context);
            String analysis = analysisResponse.getAnalysis();
            LOG.debug("[MCP] Analysis: " + analysis);

            // Step 3: Technical Answer via MCP/gRPC
            LOG.info("[MCP Pipeline] Step 3: Technical Expert (via MCP)...");
            TechnicalResponse technicalResponse = callAnswerTechnical(
                sessionId, question, context, analysis
            );
            String technicalAnswer = technicalResponse.getAnswer();
            LOG.debug("[MCP] Technical Answer: " + technicalAnswer);

            // Step 4: Validation via MCP/gRPC
            LOG.info("[MCP Pipeline] Step 4: Validator (via MCP)...");
            ValidationResponse validationResponse = callValidateResponse(
                sessionId, technicalAnswer, context, question
            );
            String validation = validationResponse.getValidationMessage();
            LOG.debug("[MCP] Validation: " + validation);

            // Step 5: Coordination via MCP/gRPC
            LOG.info("[MCP Pipeline] Step 5: Coordinator (via MCP)...");
            CoordinateResponse coordinateResponse = callCoordinateAgents(
                sessionId, question, analysis, technicalAnswer, validation
            );
            String finalAnswer = coordinateResponse.getFinalAnswer();

            long duration = System.currentTimeMillis() - startTime;
            LOG.info("[MCP Pipeline] Completed in " + duration + "ms");

            return new MCPAgentResponse(
                finalAnswer,
                analysis,
                technicalAnswer,
                validation,
                context,
                sessionId,
                "MCP+gRPC",
                duration
            );

        } catch (Exception e) {
            LOG.error("[MCP Pipeline] Error executing pipeline", e);
            throw new RuntimeException("MCP Pipeline error: " + e.getMessage(), e);
        }
    }

    /**
     * Chama o agente de análise via MCP/gRPC (in-process)
     */
    private AnalyzeResponse callAnalyzeDocument(String sessionId, String content) {
        MCPContext context = createMCPContext(sessionId, "orchestrator");

        AnalyzeRequest request = AnalyzeRequest.newBuilder()
                .setContext(context)
                .setContent(content)
                .setTask("analyze")
                .build();

        // Simulação de chamada in-process (em produção seria via gRPC real)
        AnalyzeResponseCollector collector = new AnalyzeResponseCollector();
        mcpAgentService.analyzeDocument(request, collector);
        return collector.getResponse();
    }

    /**
     * Chama o agente técnico via MCP/gRPC
     */
    private TechnicalResponse callAnswerTechnical(
            String sessionId, String question, String context, String previousAnalysis) {

        MCPContext mcpContext = createMCPContext(sessionId, "orchestrator");

        TechnicalRequest request = TechnicalRequest.newBuilder()
                .setContext(mcpContext)
                .setQuestion(question)
                .setContextContent(context)
                .setPreviousAnalysis(previousAnalysis)
                .build();

        TechnicalResponseCollector collector = new TechnicalResponseCollector();
        mcpAgentService.answerTechnical(request, collector);
        return collector.getResponse();
    }

    /**
     * Chama o agente validador via MCP/gRPC
     */
    private ValidationResponse callValidateResponse(
            String sessionId, String answer, String context, String question) {

        MCPContext mcpContext = createMCPContext(sessionId, "orchestrator");

        ValidateRequest request = ValidateRequest.newBuilder()
                .setContext(mcpContext)
                .setAnswer(answer)
                .setOriginalContext(context)
                .setQuestion(question)
                .build();

        ValidateResponseCollector collector = new ValidateResponseCollector();
        mcpAgentService.validateAnswer(request, collector);
        return collector.getResponse();
    }

    /**
     * Chama o agente coordenador via MCP/gRPC
     */
    private CoordinateResponse callCoordinateAgents(
            String sessionId, String question, String analysis,
            String technicalAnswer, String validation) {

        MCPContext mcpContext = createMCPContext(sessionId, "orchestrator");

        CoordinateRequest request = CoordinateRequest.newBuilder()
                .setContext(mcpContext)
                .setQuestion(question)
                .setAnalysis(analysis)
                .setTechnicalAnswer(technicalAnswer)
                .setValidation(validation)
                .build();

        CoordinateResponseCollector collector = new CoordinateResponseCollector();
        mcpAgentService.coordinateAgents(request, collector);
        return collector.getResponse();
    }

    /**
     * Cria contexto MCP para rastreamento
     */
    private MCPContext createMCPContext(String sessionId, String agentId) {
        return MCPContext.newBuilder()
                .setSessionId(sessionId)
                .setAgentId(agentId)
                .setTimestamp(System.currentTimeMillis())
                .putMetadata("protocol", "MCP-gRPC")
                .putMetadata("version", "1.0")
                .build();
    }

    // Collectors para resposta (simplificado para uso in-process)
    private static class AnalyzeResponseCollector implements io.grpc.stub.StreamObserver<AnalyzeResponse> {
        private AnalyzeResponse response;

        @Override
        public void onNext(AnalyzeResponse value) {
            this.response = value;
        }

        @Override
        public void onError(Throwable t) {
            throw new RuntimeException(t);
        }

        @Override
        public void onCompleted() {}

        public AnalyzeResponse getResponse() {
            return response;
        }
    }

    private static class TechnicalResponseCollector implements io.grpc.stub.StreamObserver<TechnicalResponse> {
        private TechnicalResponse response;

        @Override
        public void onNext(TechnicalResponse value) {
            this.response = value;
        }

        @Override
        public void onError(Throwable t) {
            throw new RuntimeException(t);
        }

        @Override
        public void onCompleted() {}

        public TechnicalResponse getResponse() {
            return response;
        }
    }

    private static class ValidateResponseCollector implements io.grpc.stub.StreamObserver<ValidationResponse> {
        private ValidationResponse response;

        @Override
        public void onNext(ValidationResponse value) {
            this.response = value;
        }

        @Override
        public void onError(Throwable t) {
            throw new RuntimeException(t);
        }

        @Override
        public void onCompleted() {}

        public ValidationResponse getResponse() {
            return response;
        }
    }

    private static class CoordinateResponseCollector implements io.grpc.stub.StreamObserver<CoordinateResponse> {
        private CoordinateResponse response;

        @Override
        public void onNext(CoordinateResponse value) {
            this.response = value;
        }

        @Override
        public void onError(Throwable t) {
            throw new RuntimeException(t);
        }

        @Override
        public void onCompleted() {}

        public CoordinateResponse getResponse() {
            return response;
        }
    }

    /**
     * Resposta do pipeline MCP com metadados adicionais
     */
    public record MCPAgentResponse(
        String finalAnswer,
        String documentAnalysis,
        String technicalAnswer,
        String validation,
        String retrievedContext,
        String sessionId,
        String protocol,
        long executionTimeMs
    ) {}
}
