package com.quarkus.rag.mcp.service;

import com.quarkus.rag.ai.agents.*;
import com.quarkus.rag.mcp.proto.*;
import io.grpc.stub.StreamObserver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * Implementação do serviço gRPC para comunicação entre agentes usando MCP
 * (Model Context Protocol)
 *
 * Esta é a implementação alternativa que usa gRPC para comunicação estruturada
 * entre agentes, permitindo comparação com a abordagem LangChain pura.
 */
@ApplicationScoped
public class MCPAgentService extends AgentCommunicationServiceGrpc.AgentCommunicationServiceImplBase {

    private static final Logger LOG = Logger.getLogger(MCPAgentService.class);

    @Inject
    DocumentAnalystAgent documentAnalystAgent;

    @Inject
    TechnicalExpertAgent technicalExpertAgent;

    @Inject
    ValidatorAgent validatorAgent;

    @Inject
    CoordinatorAgent coordinatorAgent;

    @Override
    public void analyzeDocument(AnalyzeRequest request, StreamObserver<AnalyzeResponse> responseObserver) {
        LOG.info("[MCP-gRPC] Analyzing document via MCP protocol");

        try {
            String content = request.getContent();
            String analysis = documentAnalystAgent.analyzeDocument(content);
            String keyInfo = documentAnalystAgent.extractKeyInformation(content);

            // Criar contexto MCP de resposta
            MCPContext responseContext = MCPContext.newBuilder()
                    .setSessionId(request.getContext().getSessionId())
                    .setAgentId("document-analyst")
                    .setTimestamp(System.currentTimeMillis())
                    .build();

            // Construir resposta MCP
            AnalyzeResponse response = AnalyzeResponse.newBuilder()
                    .setContext(responseContext)
                    .setAnalysis(analysis)
                    .setSummary(keyInfo)
                    .addKeyPoints("Analysis completed via MCP protocol")
                    .addKeyPoints("Using gRPC for inter-agent communication")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            LOG.info("[MCP-gRPC] Document analysis completed");
        } catch (Exception e) {
            LOG.error("[MCP-gRPC] Error analyzing document", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void answerTechnical(TechnicalRequest request, StreamObserver<TechnicalResponse> responseObserver) {
        LOG.info("[MCP-gRPC] Processing technical question via MCP protocol");

        try {
            String question = request.getQuestion();
            String context = request.getContextContent();
            String answer = technicalExpertAgent.answerTechnicalQuestion(context, question);

            MCPContext responseContext = MCPContext.newBuilder()
                    .setSessionId(request.getContext().getSessionId())
                    .setAgentId("technical-expert")
                    .setTimestamp(System.currentTimeMillis())
                    .build();

            TechnicalResponse response = TechnicalResponse.newBuilder()
                    .setContext(responseContext)
                    .setAnswer(answer)
                    .setConfidence(0.95)
                    .addSources("Retrieved documents")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            LOG.info("[MCP-gRPC] Technical answer completed");
        } catch (Exception e) {
            LOG.error("[MCP-gRPC] Error answering technical question", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void validateAnswer(ValidateRequest request, StreamObserver<ValidationResponse> responseObserver) {
        LOG.info("[MCP-gRPC] Validating response via MCP protocol");

        try {
            String answer = request.getAnswer();
            String context = request.getOriginalContext();
            String validation = validatorAgent.validateAnswer(context, answer);

            MCPContext responseContext = MCPContext.newBuilder()
                    .setSessionId(request.getContext().getSessionId())
                    .setAgentId("validator")
                    .setTimestamp(System.currentTimeMillis())
                    .build();

            ValidationResponse response = ValidationResponse.newBuilder()
                    .setContext(responseContext)
                    .setIsValid(true)
                    .setValidationMessage(validation)
                    .setAccuracyScore(0.92)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            LOG.info("[MCP-gRPC] Validation completed");
        } catch (Exception e) {
            LOG.error("[MCP-gRPC] Error validating response", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void coordinateAgents(CoordinateRequest request, StreamObserver<CoordinateResponse> responseObserver) {
        LOG.info("[MCP-gRPC] Coordinating agents via MCP protocol");

        try {
            String finalAnswer = coordinatorAgent.synthesizeFinalAnswer(
                    request.getAnalysis(),
                    request.getTechnicalAnswer(),
                    request.getValidation(),
                    request.getQuestion()
            );

            MCPContext responseContext = MCPContext.newBuilder()
                    .setSessionId(request.getContext().getSessionId())
                    .setAgentId("coordinator")
                    .setTimestamp(System.currentTimeMillis())
                    .build();

            CoordinateResponse response = CoordinateResponse.newBuilder()
                    .setContext(responseContext)
                    .setFinalAnswer(finalAnswer)
                    .setReasoning("Synthesized from all agent responses using MCP protocol")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            LOG.info("[MCP-gRPC] Coordination completed");
        } catch (Exception e) {
            LOG.error("[MCP-gRPC] Error coordinating agents", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public StreamObserver<AgentEvent> streamAgentEvents(StreamObserver<AgentEvent> responseObserver) {
        LOG.info("[MCP-gRPC] Starting agent event stream");

        return new StreamObserver<AgentEvent>() {
            @Override
            public void onNext(AgentEvent event) {
                LOG.info("[MCP-gRPC] Received event from: " + event.getAgentName());

                // Echo back with processing confirmation
                AgentEvent response = AgentEvent.newBuilder()
                        .setContext(event.getContext())
                        .setEventType("ACK")
                        .setAgentName("orchestrator")
                        .setMessage("Event received and processed")
                        .build();

                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable t) {
                LOG.error("[MCP-gRPC] Error in event stream", t);
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                LOG.info("[MCP-gRPC] Event stream completed");
                responseObserver.onCompleted();
            }
        };
    }
}
