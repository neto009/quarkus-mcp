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

import java.io.InputStream;
import java.util.List;

@ApplicationScoped
public class DocumentIngestionService {

    @Inject
    EmbeddingModel embeddingModel;

    @Inject
    EmbeddingStore<TextSegment> embeddingStore;

    public void ingestDocument(InputStream inputStream, String fileName, String contentType) {
        // Parse document based on type
        Document document = parseDocument(inputStream, fileName, contentType);

        // Split document into segments
        DocumentSplitter splitter = DocumentSplitters.recursive(
            1000,  // maxSegmentSize
            200    // maxOverlapSize
        );

        List<TextSegment> segments = splitter.split(document);

        // Create ingestor and ingest
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
            .embeddingModel(embeddingModel)
            .embeddingStore(embeddingStore)
            .documentSplitter(splitter)
            .build();

        ingestor.ingest(document);
    }

    private Document parseDocument(InputStream inputStream, String fileName, String contentType) {
        return switch (contentType.toLowerCase()) {
            case "application/pdf" -> new ApachePdfBoxDocumentParser().parse(inputStream);
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                 "application/msword" -> new ApachePoiDocumentParser().parse(inputStream);
            case "text/plain" -> new TextDocumentParser().parse(inputStream);
            default -> {
                // Try text parser as fallback
                yield new TextDocumentParser().parse(inputStream);
            }
        };
    }
}

