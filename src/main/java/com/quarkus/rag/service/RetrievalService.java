package com.quarkus.rag.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class RetrievalService {

    @Inject
    EmbeddingStore<TextSegment> embeddingStore;

    @Inject
    EmbeddingModel embeddingModel;

    public List<String> retrieve(String query, int maxResults) {
        // Embed the query
        var queryEmbedding = embeddingModel.embed(query).content();

        // Search for similar segments
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(
            queryEmbedding,
            maxResults,
            0.7  // minScore - relevance threshold
        );

        // Extract text from matches
        return matches.stream()
            .map(match -> match.embedded().text())
            .collect(Collectors.toList());
    }
}

