package com.demo.ai.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a document stored in MongoDB for reference tracking. The actual vector embeddings are stored separately in
 * the embeddings collection managed by LangChain4j's MongoDbEmbeddingStore.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ingested_documents")
public class KnowledgeDocument {

    @Id
    private String id;

    private String title;

    /**
     * The raw text content
     */
    private String content;

    /**
     * Category/tag for filtering
     */
    private String category;

    /**
     * Number of chunks this document was split into
     */
    private int chunkCount;

    private LocalDateTime ingestedAt;
}
