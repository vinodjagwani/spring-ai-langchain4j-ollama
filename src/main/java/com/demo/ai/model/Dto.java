package com.demo.ai.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTOs (Data Transfer Objects) for the REST API
 */
public class Dto {

    // ─── Ingest ───────────────────────────────────────────────

    @Data
    public static class IngestRequest {

        @NotBlank(message = "Title is required")
        private String title;

        @NotBlank(message = "Content is required")
        private String content;

        private String category = "general";
    }

    @Data
    public static class IngestResponse {

        private String documentId;
        private String title;
        private int chunksCreated;
        private String message;

        public static IngestResponse of(String id, String title, int chunks) {
            IngestResponse r = new IngestResponse();
            r.documentId = id;
            r.title = title;
            r.chunksCreated = chunks;
            r.message = "Document ingested successfully. " + chunks + " vector chunks created.";
            return r;
        }
    }

    // ─── Query ────────────────────────────────────────────────

    @Data
    public static class QueryRequest {

        @NotBlank(message = "Question is required")
        private String question;
    }

    @Data
    public static class QueryResponse {

        private String question;
        private String answer;
        private long processingTimeMs;

        public static QueryResponse of(String question, String answer, long ms) {
            QueryResponse r = new QueryResponse();
            r.question = question;
            r.answer = answer;
            r.processingTimeMs = ms;
            return r;
        }
    }

    // ─── Similar Search ───────────────────────────────────────

    @Data
    public static class SearchRequest {

        @NotBlank
        private String query;
        private int maxResults = 5;
    }

    @Data
    public static class SearchResult {

        private String text;
        private double score;
        private String source;
    }
}
