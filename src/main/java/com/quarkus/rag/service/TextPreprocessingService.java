package com.quarkus.rag.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.jboss.logging.Logger;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Serviço de pré-processamento de texto para melhorar a qualidade dos embeddings.
 *
 * Realiza:
 * - Validação e conversão para UTF-8
 * - Remoção de caracteres especiais e HTML
 * - Normalização de espaços em branco
 * - Remoção de stopwords em português
 * - Normalização de acentos (opcional)
 * - Conversão para lowercase
 * - Stemming em português
 */
@ApplicationScoped
public class TextPreprocessingService {

    private static final Logger LOG = Logger.getLogger(TextPreprocessingService.class);

    // Analyzer do Lucene para português (inclui stopwords e stemming)
    private final Analyzer brazilianAnalyzer = new BrazilianAnalyzer();

    // Padrões regex para limpeza
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");
    private static final Pattern SPECIAL_CHARS = Pattern.compile("[^\\p{L}\\p{N}\\s.,;:!?\\-]");
    private static final Pattern HTML_TAGS = Pattern.compile("<[^>]*>");

    /**
     * Pré-processa texto para embeddings.
     * Aplica todas as transformações necessárias.
     */
    public String preprocessForEmbedding(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        try {
            // 1. Garantir UTF-8
            text = ensureUTF8(text);

            // 2. Remover HTML tags
            text = removeHtmlTags(text);

            // 3. Normalizar caracteres especiais e acentos
            text = normalizeText(text);

            // 4. Remover caracteres especiais mantendo pontuação básica
            text = removeSpecialCharacters(text);

            // 5. Normalizar espaços
            text = normalizeWhitespace(text);

            // 6. Aplicar análise Lucene (lowercase, stopwords, stemming)
            text = applyLuceneAnalysis(text);

            LOG.debugf("Text preprocessed successfully. Length: %d", text.length());

            return text.trim();
        } catch (Exception e) {
            LOG.errorf(e, "Error preprocessing text: %s", e.getMessage());
            // Em caso de erro, retorna texto básico limpo
            return normalizeWhitespace(text).trim();
        }
    }

    /**
     * Pré-processa apenas para busca (mantém mais contexto).
     * Usado para processar a pergunta do usuário.
     */
    public String preprocessForQuery(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }

        try {
            // 1. Garantir UTF-8
            query = ensureUTF8(query);

            // 2. Normalizar caracteres
            query = normalizeText(query);

            // 3. Normalizar espaços
            query = normalizeWhitespace(query);

            // 4. Aplicar análise Lucene
            query = applyLuceneAnalysis(query);

            return query.trim();
        } catch (Exception e) {
            LOG.errorf(e, "Error preprocessing query: %s", e.getMessage());
            return normalizeWhitespace(query).trim();
        }
    }

    /**
     * Garante que o texto está em UTF-8.
     */
    private String ensureUTF8(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Remove tags HTML.
     */
    private String removeHtmlTags(String text) {
        return HTML_TAGS.matcher(text).replaceAll(" ");
    }

    /**
     * Normaliza o texto removendo acentos e caracteres especiais Unicode.
     */
    private String normalizeText(String text) {
        // Normaliza Unicode (NFD = decomposição canônica)
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        // Remove marcas diacríticas, mas mantém letras base
        // Nota: Para embeddings, manter acentos pode ser útil em português
        // então vamos apenas normalizar a forma
        return Normalizer.normalize(text, Normalizer.Form.NFC);
    }

    /**
     * Remove caracteres especiais mantendo pontuação básica.
     */
    private String removeSpecialCharacters(String text) {
        // Remove caracteres que não são letras, números ou pontuação básica
        return SPECIAL_CHARS.matcher(text).replaceAll(" ");
    }

    /**
     * Normaliza espaços em branco (múltiplos espaços, tabs, newlines).
     */
    private String normalizeWhitespace(String text) {
        return MULTIPLE_SPACES.matcher(text).replaceAll(" ");
    }

    /**
     * Aplica análise do Lucene: tokenização, lowercase, remoção de stopwords e stemming.
     */
    private String applyLuceneAnalysis(String text) {
        List<String> tokens = new ArrayList<>();

        try (TokenStream tokenStream = brazilianAnalyzer.tokenStream("content", new StringReader(text))) {
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();

            while (tokenStream.incrementToken()) {
                String token = charTermAttribute.toString();
                // Adiciona apenas tokens com tamanho mínimo (evita tokens muito pequenos)
                if (token.length() >= 2) {
                    tokens.add(token);
                }
            }
            tokenStream.end();
        } catch (Exception e) {
            LOG.errorf(e, "Error applying Lucene analysis: %s", e.getMessage());
            // Fallback: retorna texto original apenas em lowercase
            return text.toLowerCase();
        }
        return String.join(" ", tokens);
    }

    /**
     * Valida se o texto tem tamanho adequado para embedding.
     */
    public boolean isValidForEmbedding(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String processed = preprocessForEmbedding(text);
        // Mínimo de 10 caracteres após processamento
        return processed.length() >= 10;
    }
}

