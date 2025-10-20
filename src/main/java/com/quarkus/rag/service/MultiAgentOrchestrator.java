package com.quarkus.rag.service;

import com.quarkus.rag.ai.agents.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Orquestrador de Multi-Agentes (Agent-to-Agent)
 *
 * Este serviço coordena múltiplos agentes especializados que trabalham em conjunto
 * para fornecer respostas mais precisas e completas.
 *
 * Fluxo A2A:
 * 1. Retrieval - Busca documentos relevantes
 * 2. DocumentAnalyst - Analisa e sumariza os documentos
 * 3. TechnicalExpert - Responde a pergunta técnica
 * 4. Validator - Valida a resposta
 * 5. Coordinator - Sintetiza tudo em uma resposta final
 */
@ApplicationScoped
public class MultiAgentOrchestrator {

    private static final Logger LOG = Logger.getLogger(MultiAgentOrchestrator.class);

    @Inject
    RetrievalService retrievalService;

    @Inject
    DocumentAnalystAgent documentAnalystAgent;

    @Inject
    TechnicalExpertAgent technicalExpertAgent;

    @Inject
    ValidatorAgent validatorAgent;

    @Inject
    CoordinatorAgent coordinatorAgent;

    /**
     * Executa o pipeline completo de multi-agentes
     */
    public AgentResponse executeAgentPipeline(String question, int maxResults) {
        LOG.info("Starting multi-agent pipeline for question: " + question);

        // Step 1: Retrieve relevant documents
        LOG.info("[Agent Pipeline] Step 1: Retrieving documents...");
        List<String> relevantDocs = retrievalService.retrieve(question, maxResults);

        if (relevantDocs.isEmpty()) {
            return new AgentResponse(
                "Não encontrei documentos relevantes para responder sua pergunta.",
                null, null, null, null
            );
        }

        String context = String.join("\n\n", relevantDocs);

        // Step 2: Document Analyst analyzes the retrieved content
        LOG.info("[Agent Pipeline] Step 2: Document Analyst analyzing content...");
        String analysis = documentAnalystAgent.analyzeDocument(context);
        LOG.debug("Analysis: " + analysis);

        // Step 3: Technical Expert answers the question
        LOG.info("[Agent Pipeline] Step 3: Technical Expert answering question...");
        String technicalAnswer = technicalExpertAgent.answerTechnicalQuestion(context, question);
        LOG.debug("Technical Answer: " + technicalAnswer);

        // Step 4: Validator checks the answer
        LOG.info("[Agent Pipeline] Step 4: Validator checking answer...");
        String validation = validatorAgent.validateAnswer(context, technicalAnswer);
        LOG.debug("Validation: " + validation);

        // Step 5: Coordinator synthesizes everything
        LOG.info("[Agent Pipeline] Step 5: Coordinator synthesizing final answer...");
        String finalAnswer = coordinatorAgent.synthesizeFinalAnswer(
            analysis,
            technicalAnswer,
            validation,
            question
        );

        LOG.info("Multi-agent pipeline completed successfully");

        return new AgentResponse(finalAnswer, analysis, technicalAnswer, validation, context);
    }

    /**
     * Execução simplificada - apenas análise e resposta
     */
    public String executeSimplified(String question, int maxResults) {
        List<String> relevantDocs = retrievalService.retrieve(question, maxResults);

        if (relevantDocs.isEmpty()) {
            return "Não encontrei documentos relevantes.";
        }

        String context = String.join("\n\n", relevantDocs);
        return technicalExpertAgent.answerTechnicalQuestion(context, question);
    }

    /**
     * Análise de documento por múltiplos agentes
     */
    public DocumentAnalysisResult analyzeWithAgents(String documentContent) {
        LOG.info("Starting multi-agent document analysis");

        // Analyst extracts key information
        String keyInfo = documentAnalystAgent.extractKeyInformation(documentContent);

        // Analyst creates summary
        String summary = documentAnalystAgent.analyzeDocument(documentContent);

        return new DocumentAnalysisResult(summary, keyInfo);
    }

    /**
     * Resposta de um agente com dados intermediários
     */
    public record AgentResponse(
        String finalAnswer,
        String documentAnalysis,
        String technicalAnswer,
        String validation,
        String retrievedContext
    ) {}

    public record DocumentAnalysisResult(
        String summary,
        String keyInformation
    ) {}
}

