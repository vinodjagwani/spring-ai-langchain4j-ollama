package com.demo.ai.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Defines and wires the AI Assistant using LangChain4j AiServices.
 * <p>
 * AiServices is LangChain4j's magic: you define a Java interface, and LangChain4j generates the implementation at
 * runtime (similar to Spring Data JPA repos).
 * <p>
 * The RAG (Retrieval-Augmented Generation) pipeline: User Question ↓ [1] Embed question → float[1536] ↓ [2] Vector
 * search MongoDB → top-k relevant chunks ↓ [3] Inject chunks into prompt as context ↓ [4] Send augmented prompt to GPT
 * → answer
 */
@Configuration
public class AssistantConfig {

    @Bean
    public DocumentAssistant documentAssistant(
            ChatModel chatModel,
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore) {

        // ContentRetriever: on each query, embeds the question and
        // fetches the top 5 most similar chunks from MongoDB
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.75)  // Filters out low-relevance chunks — keeps only closely matching context
                .build();

        // AiServices wires everything together:
        // RAG retriever + chat model + system prompt
        return AiServices.builder(DocumentAssistant.class)
                .chatModel(chatModel)
                .contentRetriever(contentRetriever)
                .build();
    }

    /**
     * The AI Assistant interface — LangChain4j generates the implementation.
     */
    public interface DocumentAssistant {

        @SystemMessage("""
                Answer the question using the context provided. Be direct and brief. Do not comment on the topics.
                """)
        String chat(String userMessage);
    }
}
