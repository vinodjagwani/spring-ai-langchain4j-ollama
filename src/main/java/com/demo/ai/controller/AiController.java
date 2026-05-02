package com.demo.ai.controller;

import com.demo.ai.model.Dto;
import com.demo.ai.model.KnowledgeDocument;
import com.demo.ai.repository.DocumentRepository;
import com.demo.ai.service.ChatService;
import com.demo.ai.service.DocumentIngestionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for the AI Vector Search demo.
 * <p>
 * Endpoints: POST   /api/documents/ingest       — Add a document to the knowledge base GET    /api/documents
 *   — List all ingested documents POST   /api/chat/ask               — Ask a question (RAG pipeline) POST
 * /api/search/similar         — Raw vector similarity search GET    /api/health                 — Health check
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")  // Enable CORS for demo purposes
public class AiController {

    private final DocumentIngestionService ingestionService;
    private final ChatService chatService;
    private final DocumentRepository documentRepository;

    // ─── Health ─────────────────────────────────────────────────

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "AI Vector Search Demo"
        ));
    }

    // ─── Document Ingestion ─────────────────────────────────────

    /**
     * Ingest a document into the knowledge base.
     * <p>
     * Example body: { "title": "Spring Boot Guide", "content": "Spring Boot makes it easy to create...", "category":
     * "technology" }
     */
    @PostMapping("/documents/ingest")
    public ResponseEntity<Dto.IngestResponse> ingestDocument(
            @Valid @RequestBody Dto.IngestRequest request) {

        Dto.IngestResponse response = ingestionService.ingest(request);
        return ResponseEntity.ok(response);
    }

    /**
     * List all ingested document metadata records.
     */
    @GetMapping("/documents")
    public ResponseEntity<List<KnowledgeDocument>> listDocuments() {
        return ResponseEntity.ok(documentRepository.findAll());
    }

    // ─── RAG Chat ────────────────────────────────────────────────

    /**
     * Ask a question — triggers the full RAG pipeline: embed → vector search → augment prompt → GPT answer
     * <p>
     * Example body: { "question": "How do I configure Spring Boot?" }
     */
    @PostMapping("/chat/ask")
    public ResponseEntity<Dto.QueryResponse> ask(
            @Valid @RequestBody Dto.QueryRequest request) {

        return ResponseEntity.ok(chatService.ask(request));
    }

    // ─── Raw Vector Search ────────────────────────────────────────

    /**
     * Perform a raw semantic similarity search without LLM generation. Useful for debugging what the retriever finds.
     * <p>
     * Example body: { "query": "dependency injection", "maxResults": 3 }
     */
    @PostMapping("/search/similar")
    public ResponseEntity<List<Dto.SearchResult>> searchSimilar(
            @Valid @RequestBody Dto.SearchRequest request) {

        List<Dto.SearchResult> results =
                ingestionService.findSimilar(request.getQuery(), request.getMaxResults());
        return ResponseEntity.ok(results);
    }
}
