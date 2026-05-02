package com.demo.ai.service;

import com.demo.ai.config.AssistantConfig.DocumentAssistant;
import com.demo.ai.model.Dto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * ═══════════════════════════════════════════════════════════════ CHAT SERVICE — RAG Query Phase
 * ═══════════════════════════════════════════════════════════════
 * <p>
 * This service handles the "query" phase of RAG:
 * <p>
 * USER QUESTION │ ▼ ┌──────────────────┐ │  EMBED QUESTION  │  "What is LangChain4j?" → float[1536]
 * └────────┬─────────┘ │ ▼ ┌──────────────────┐ │  VECTOR SEARCH   │  MongoDB $vectorSearch → top-5 chunks │  (MongoDB
 * Atlas) │  similarity score ≥ 0.7 └────────┬─────────┘ │  [chunk1 (0.92), chunk2 (0.88), chunk3 (0.81)] ▼
 * ┌──────────────────────────────────────────┐ │  AUGMENTED PROMPT                        │ │  System: "Answer based on
 * context only"  │ │  Context: chunk1 + chunk2 + chunk3       │ │  User: "What is LangChain4j?"            │
 * └────────┬─────────────────────────────────┘ │ ▼ ┌──────────────────┐ │  GPT-4o-mini     │  Generates grounded answer
 * └────────┬─────────┘ │ ▼ "LangChain4j is a Java library for building..."
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    // LangChain4j-generated implementation with RAG pipeline wired in
    private final DocumentAssistant documentAssistant;

    public Dto.QueryResponse ask(Dto.QueryRequest request) {
        log.info("Processing question: {}", request.getQuestion());

        long start = System.currentTimeMillis();

        // LangChain4j handles the full RAG pipeline:
        // embed → search → augment prompt → call GPT → return answer
        String answer = documentAssistant.chat(request.getQuestion());

        long elapsed = System.currentTimeMillis() - start;
        log.info("Answer generated in {}ms", elapsed);

        return Dto.QueryResponse.of(request.getQuestion(), answer, elapsed);
    }
}
