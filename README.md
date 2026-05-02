# Spring Boot + LangChain4j + MongoDB Atlas + Ollama — Local RAG Demo

A complete RAG (Retrieval-Augmented Generation) application that runs entirely on your machine. No API keys, no cloud costs, no data leaving your environment.

## Tech Stack

- **Spring Boot 3.3.5** with Java 21
- **LangChain4j 1.0.0** for AI orchestration
- **MongoDB Atlas M0** (free tier) for vector storage and search
- **Ollama** for local LLM and embedding models
    - `llama3.2` — 3B parameter chat model by Meta, handles answer generation
    - `nomic-embed-text` — embedding model by Nomic AI, produces 768-dimensional vectors

## Architecture

```
INGESTION PHASE

  Your Document
       |
  Document Splitter (500 tokens, 50 overlap)
       |
  Ollama: nomic-embed-text → float[768]
       |
  MongoDB Atlas Vector Store


QUERY PHASE

  User Question
       |
  Ollama: nomic-embed-text → float[768]
       |
  MongoDB $vectorSearch (top 3, score >= 0.75)
       |
  LangChain4j: question + retrieved chunks → prompt
       |
  Ollama: llama3.2 → answer
```

## Prerequisites

- Java 21
- Maven 3.8+
- MongoDB Atlas free account — https://mongodb.com/atlas
- Ollama — https://ollama.com/download/windows

## Setup

### 1. Install Ollama and pull models

Download and install Ollama from https://ollama.com/download/windows, then run:

```bash
ollama pull llama3.2
ollama pull nomic-embed-text
```

### 2. Configure MongoDB Atlas

1. Create a free M0 cluster at https://mongodb.com/atlas
2. Create a database user with read/write access
3. Whitelist your IP in Network Access
4. Copy the connection string into `application.yml`

### 3. Configure application.yml

```yaml
spring:
  data:
    mongodb:
      uri: mongodb+srv://youruser:yourpassword@cluster0.xxxxx.mongodb.net/ai_demo

langchain4j:
  ollama:
    chat-model:
      base-url: http://localhost:11434
      model-name: llama3.2
      temperature: 0.7
    embedding-model:
      base-url: http://localhost:11434
      model-name: nomic-embed-text

app:
  vector-store:
    collection-name: documents
    index-name: vector_index
```

### 4. Run

```bash
mvn spring-boot:run
```

On first startup the app auto-creates the vector search index in MongoDB Atlas:

```
INFO - Creating vector search index 'vector_index' on collection 'documents'
```

## API Endpoints

### Health Check
```bash
curl http://localhost:8080/api/health
```

```json
{ "status": "UP", "service": "AI Vector Search Demo" }
```

### Ingest a Document
```bash
curl -X POST http://localhost:8080/api/documents/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Photosynthesis Guide",
    "category": "biology",
    "content": "Photosynthesis is the process used by plants to convert light energy into chemical energy stored in glucose..."
  }'
```

```json
{
  "documentId": "683a1f2c9b4e1a00087d3f21",
  "title": "Photosynthesis Guide",
  "chunksCreated": 2,
  "message": "Document ingested successfully. 2 vector chunks created."
}
```

### Ask a Question (Full RAG Pipeline)
```bash
curl -X POST http://localhost:8080/api/chat/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "How do plants make food from sunlight?"}'
```

```json
{
  "question": "How do plants make food from sunlight?",
  "answer": "Plants make food (glucose) from sunlight through photosynthesis...",
  "processingTimeMs": 10495
}
```

### Raw Vector Similarity Search (No LLM)
```bash
curl -X POST http://localhost:8080/api/search/similar \
  -H "Content-Type: application/json" \
  -d '{"query": "how plants get energy", "maxResults": 3}'
```

```json
[
  {
    "text": "Photosynthesis is the process used by plants...",
    "score": 0.87,
    "source": "Photosynthesis Guide"
  }
]
```

Use this endpoint to debug retrieval before debugging the model. If the scores look wrong, adjust `minScore` in `AssistantConfig.java`.

### List All Documents
```bash
curl http://localhost:8080/api/documents
```

## How It Works

### Ingestion Phase
1. Text is split into 500-token chunks with 50-token overlap
2. Each chunk is sent to `nomic-embed-text` via Ollama and returns `float[768]`
3. Vectors and original text are stored in MongoDB Atlas

### Query Phase
1. Question is converted to `float[768]` by `nomic-embed-text`
2. MongoDB `$vectorSearch` finds top 3 chunks with cosine similarity >= 0.75
3. Retrieved chunks are injected into the prompt as context
4. `llama3.2` generates a grounded answer from the context

## Key Configuration Notes

**minScore** in `AssistantConfig.java` controls which retrieved chunks get passed to the LLM. Set too low and irrelevant chunks confuse the model. Set too high and valid chunks get filtered out. Use the `/api/search/similar` endpoint to check actual similarity scores for your data before tuning this value.

**dimension** in `VectorStoreConfig.java` must match your embedding model. `nomic-embed-text` produces 768 dimensions. If you switch embedding models, update this value.

## Switching to a Cloud Provider

Changing from Ollama to OpenAI or any other provider only requires updating `application.yml`. No code changes needed.

## Project Structure

```
src/main/java/com/demo/ai/
├── AiVectorSearchApplication.java       # Entry point
├── config/
│   ├── VectorStoreConfig.java           # MongoDB vector store bean (768 dims)
│   ├── AssistantConfig.java             # LangChain4j AiService + RAG pipeline
│   └── DataSeeder.java                  # Optional demo data seeder
├── controller/
│   └── AiController.java                # REST endpoints
├── service/
│   ├── DocumentIngestionService.java    # Chunk, embed and store
│   └── ChatService.java                 # RAG query pipeline
├── model/
│   ├── KnowledgeDocument.java           # MongoDB document entity
│   └── Dto.java                         # Request and response DTOs
└── repository/
    └── DocumentRepository.java          # Spring Data MongoDB repo
```
