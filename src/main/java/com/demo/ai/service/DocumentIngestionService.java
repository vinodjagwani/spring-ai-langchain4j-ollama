package com.demo.ai.service;

import com.demo.ai.model.Dto;
import com.demo.ai.model.KnowledgeDocument;
import com.demo.ai.repository.DocumentRepository;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * ═══════════════════════════════════════════════════════════════ DOCUMENT INGESTION SERVICE
 * ═══════════════════════════════════════════════════════════════
 * <p>
 * This service handles the "indexing" phase of RAG:
 * <p>
 * TEXT DOCUMENT │ ▼ ┌──────────────┐ │  CHUNKING    │  Split text into overlapping chunks │  (splitter)  │  e.g., 500
 * tokens, 50 token overlap └──────┬───────┘ │  [chunk1, chunk2, chunk3, ...] ▼ ┌──────────────┐ │  EMBEDDING   │  Send
 * each chunk to OpenAI text-embedding-3-small │  MODEL       │  → float[1536] for each chunk └──────┬───────┘ │
 * [(vector1, chunk1), (vector2, chunk2), ...] ▼ ┌──────────────┐ │  MONGODB     │  Store vectors + text in Atlas
 * collection │  ATLAS       │  with vector search index └──────────────┘
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIngestionService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final DocumentRepository documentRepository;

    /**
     * Ingest a document into the vector store.
     * <p>
     * Steps: 1. Split document into chunks 2. Embed each chunk using OpenAI 3. Store embeddings + text in MongoDB Atlas
     * 4. Save metadata record to MongoDB
     */
    public Dto.IngestResponse ingest(Dto.IngestRequest request) {
        log.info("Starting ingestion of document: {}", request.getTitle());

        // ── Step 1: Wrap content in LangChain4j Document ──────────
        Document document = Document.from(
                request.getContent(),
                Metadata.from("title", request.getTitle())
                        .put("category", request.getCategory())
        );

        // ── Step 2: Split into chunks ──────────────────────────────
        // DocumentSplitters.recursive splits on paragraphs → sentences → words
        // 500 tokens per chunk, 50 token overlap (for context continuity)
        DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);
        List<TextSegment> segments = splitter.split(document);

        log.info("Split into {} chunks", segments.size());

        // ── Step 3: Embed all chunks (batch call to OpenAI) ────────
        // Each TextSegment → float[1536] (1536-dimensional vector)
        List<Embedding> embeddings = new ArrayList<>();
        for (TextSegment segment : segments) {
            Response<Embedding> embeddingResponse = embeddingModel.embed(segment);
            embeddings.add(embeddingResponse.content());
        }

        // ── Step 4: Store in MongoDB Atlas Vector Store ────────────
        // Stored as BSON document: { text: "...", embedding: [0.1, 0.2, ...], metadata: {...} }
        embeddingStore.addAll(embeddings, segments);

        // ── Step 5: Save metadata record ──────────────────────────
        KnowledgeDocument docRecord = KnowledgeDocument.builder()
                .title(request.getTitle())
                .content(request.getContent().substring(0, Math.min(200, request.getContent().length())) + "...")
                .category(request.getCategory())
                .chunkCount(segments.size())
                .ingestedAt(LocalDateTime.now())
                .build();

        KnowledgeDocument saved = documentRepository.save(docRecord);

        log.info("Ingestion complete. Document ID: {}", saved.getId());
        return Dto.IngestResponse.of(saved.getId(), request.getTitle(), segments.size());
    }

    /**
     * Find similar documents using pure vector similarity search. This does NOT go through the LLM — it's a raw
     * semantic search.
     * <p>
     * Query Text → Embed → float[1536] ↓ MongoDB $vectorSearch ↓ Top-K results by cosine similarity
     */
    public List<Dto.SearchResult> findSimilar(String query, int maxResults) {
        log.info("Searching for: '{}' (max {} results)", query, maxResults);

        // Embed the search query
        Response<Embedding> queryEmbedding = embeddingModel.embed(query);

        // Search MongoDB Atlas using vector similarity
        List<EmbeddingMatch<TextSegment>> matches =
                embeddingStore.search(EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding.content())
                        .maxResults(maxResults)
                        .build()).matches();

        // Map to response DTOs
        return matches.stream().map(match -> {
            Dto.SearchResult result = new Dto.SearchResult();
            result.setText(match.embedded().text());
            result.setScore(match.score());  // cosine similarity 0-1
            result.setSource(match.embedded().metadata().getString("title"));
            return result;
        }).toList();
    }
}
