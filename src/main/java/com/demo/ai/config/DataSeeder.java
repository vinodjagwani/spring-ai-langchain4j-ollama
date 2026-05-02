package com.demo.ai.config;

import com.demo.ai.model.Dto;
import com.demo.ai.service.DocumentIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Seeds demo data when running with the "seed" Spring profile. Run with: java -jar app.jar
 * --spring.profiles.active=seed
 * <p>
 * Or add to application.yml: spring.profiles.active: seed
 */
@Slf4j
@Configuration
@Profile("seed")
@RequiredArgsConstructor
public class DataSeeder {

    @Bean
    CommandLineRunner seedDemoData(DocumentIngestionService ingestionService) {
        return args -> {
            log.info("=== Seeding demo documents ===");

            // Document 1: Spring Boot
            Dto.IngestRequest doc1 = new Dto.IngestRequest();
            doc1.setTitle("Introduction to Spring Boot");
            doc1.setCategory("technology");
            doc1.setContent("""
                    Spring Boot is an open-source Java framework that makes it easy to create 
                    stand-alone, production-grade Spring-based applications. It takes an opinionated 
                    view of the Spring platform and third-party libraries, letting you get started 
                    with minimum fuss.
                    
                    Spring Boot auto-configures your application based on the libraries you have 
                    on the classpath. For example, if you add spring-boot-starter-web, it 
                    automatically sets up a Tomcat server and Spring MVC.
                    
                    Key features include:
                    - Auto-configuration: Spring Boot configures itself based on your dependencies
                    - Embedded servers: No need to deploy WAR files; apps run as self-contained JARs
                    - Spring Boot Actuator: Production-ready features like health checks and metrics
                    - Spring Boot DevTools: Live reload during development
                    
                    To create a Spring Boot application, you annotate your main class with 
                    @SpringBootApplication, which combines @Configuration, @EnableAutoConfiguration, 
                    and @ComponentScan.
                    """);
            ingestionService.ingest(doc1);

            // Document 2: MongoDB
            Dto.IngestRequest doc2 = new Dto.IngestRequest();
            doc2.setTitle("MongoDB Atlas Vector Search");
            doc2.setCategory("database");
            doc2.setContent("""
                    MongoDB Atlas Vector Search enables you to build semantic search and AI-powered 
                    applications using vector embeddings stored in MongoDB Atlas.
                    
                    Vector Search uses the HNSW (Hierarchical Navigable Small World) algorithm, 
                    which provides approximate nearest neighbor search with high accuracy and speed.
                    
                    To use Vector Search, you need to:
                    1. Create a vector search index on your collection specifying the embedding field 
                       dimensions and similarity metric (cosine, euclidean, or dotProduct)
                    2. Store documents with an embedding field containing the float array
                    3. Use the $vectorSearch aggregation pipeline operator to search
                    
                    The $vectorSearch operator supports:
                    - numCandidates: number of approximate candidates to consider
                    - limit: number of results to return
                    - filter: pre-filtering with standard MongoDB queries
                    - index: the name of the vector search index
                    
                    Cosine similarity is most commonly used for text embeddings as it measures 
                    the angle between vectors, making it robust to magnitude differences.
                    """);
            ingestionService.ingest(doc2);

            // Document 3: LangChain4j
            Dto.IngestRequest doc3 = new Dto.IngestRequest();
            doc3.setTitle("LangChain4j Framework Guide");
            doc3.setCategory("technology");
            doc3.setContent("""
                    LangChain4j is a Java library for building LLM-powered applications. It provides 
                    abstractions for common AI patterns like RAG (Retrieval-Augmented Generation), 
                    chat memory, AI services, and tool use.
                    
                    Core concepts:
                    
                    EmbeddingModel: Converts text into vector representations (float arrays).
                    Supported providers: OpenAI, Azure OpenAI, Hugging Face, Ollama (local).
                    
                    EmbeddingStore: Stores and retrieves embeddings using vector similarity search.
                    Supported stores: MongoDB Atlas, Pinecone, Weaviate, Chroma, Redis, pgvector.
                    
                    AiServices: A powerful abstraction where you define a Java interface, 
                    and LangChain4j generates the implementation at runtime, similar to 
                    Spring Data repositories. Supports automatic RAG integration, chat memory,
                    and tool/function calling.
                    
                    DocumentSplitters: Splits large documents into chunks for embedding.
                    RecursiveSplitter splits by paragraphs, then sentences, then words,
                    ensuring chunks don't exceed the token limit.
                    
                    ContentRetriever: Used in RAG to fetch relevant context before sending to LLM.
                    EmbeddingStoreContentRetriever embeds the query and searches the vector store.
                    """);
            ingestionService.ingest(doc3);

            log.info("=== Demo data seeded successfully ===");
            log.info("Try asking: 'What is Spring Boot auto-configuration?'");
            log.info("Try asking: 'How does MongoDB Vector Search work?'");
            log.info("Try asking: 'What are the core concepts of LangChain4j?'");
        };
    }
}
