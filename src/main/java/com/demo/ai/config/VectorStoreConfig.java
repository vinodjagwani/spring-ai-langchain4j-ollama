package com.demo.ai.config;

import com.mongodb.client.MongoClient;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.mongodb.IndexMapping;
import dev.langchain4j.store.embedding.mongodb.MongoDbEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the MongoDB Atlas Vector Store.
 * <p>
 * MongoDB Atlas Vector Search uses a special index type (vectorSearch) that allows cosine/euclidean similarity search
 * over embedding vectors.
 * <p>
 * Prerequisites on MongoDB Atlas: 1. Create a cluster (M0 free tier works) 2. Create a Vector Search index on the
 * collection: { "fields": [{ "type": "vector", "path": "embedding", "numDimensions": 1536, "similarity": "cosine" }] }
 */
@Configuration
public class VectorStoreConfig {

    @Value("${app.vector-store.database-name:ai_demo}")
    private String database;

    @Value("${app.vector-store.collection-name}")
    private String collectionName;

    @Value("${app.vector-store.index-name}")
    private String indexName;

    /**
     * MongoDbEmbeddingStore — the bridge between LangChain4j and MongoDB Atlas Vector Search.
     * <p>
     * When you call store.add(embedding, textSegment): - The embedding (float[]) is stored as a BSON array field
     * "embedding" - The text and metadata are stored alongside it
     * <p>
     * When you call store.findRelevant(queryEmbedding, maxResults): - MongoDB runs a $vectorSearch aggregation pipeline
     * - Returns top-k documents by cosine similarity
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(MongoClient mongoClient) {
        return MongoDbEmbeddingStore.builder()
                .fromClient(mongoClient)
                .databaseName(database)
                .collectionName(collectionName)
                .indexName(indexName)
                .indexMapping(IndexMapping.builder()
                        .dimension(768)
                        .metadataFieldNames(java.util.Set.of("title", "category"))
                        .build())
                .createIndex(true)
                .build();
    }
}
