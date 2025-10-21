package com.quarkus.rag.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.parser.apache.poi.ApachePoiDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.InputStream;
import java.util.List;

@ApplicationScoped
public class DocumentIngestionService {

    @Inject
    EmbeddingModel embeddingModel;

    @Inject
    EmbeddingStore<TextSegment> embeddingStore;

    @Inject
    TextPreprocessingService textPreprocessingService;

    @ConfigProperty(name = "quarkus.langchain4j.easy-rag.max-segment-size", defaultValue = "1000")
    int maxSegmentSize;

    @ConfigProperty(name = "quarkus.langchain4j.easy-rag.max-overlap-size", defaultValue = "200")
    int maxOverlapSize;

    public void ingestDocument(InputStream inputStream, String fileName, String contentType) {
        // Parse document based on type
        Document document = parseDocument(inputStream, fileName, contentType);

        // Pré-processar o texto do documento
        String originalText = document.text();
        String preprocessedText = textPreprocessingService.preprocessForEmbedding(originalText);

        // Validar se o texto processado é adequado
        if (!textPreprocessingService.isValidForEmbedding(preprocessedText)) {
            throw new IllegalArgumentException("Documento não contém texto válido após pré-processamento");
        }

        // Criar novo documento com texto pré-processado
        Document processedDocument = Document.from(preprocessedText, document.metadata());

        // Split document into segments using configured values
        DocumentSplitter splitter = DocumentSplitters.recursive(
            maxSegmentSize,
            maxOverlapSize
        );

        // Create ingestor and ingest
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
            .embeddingModel(embeddingModel)
            .embeddingStore(embeddingStore)
            .documentSplitter(splitter)
            .build();

        ingestor.ingest(processedDocument);
    }

    private Document parseDocument(InputStream inputStream, String fileName, String contentType) {
        return switch (contentType.toLowerCase()) {
            case "application/pdf" -> new ApachePdfBoxDocumentParser().parse(inputStream);
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                 "application/msword" -> new ApachePoiDocumentParser().parse(inputStream);
            case "text/plain" -> new TextDocumentParser().parse(inputStream);
            default -> {
                yield new TextDocumentParser().parse(inputStream);
            }
        };
    }
}

