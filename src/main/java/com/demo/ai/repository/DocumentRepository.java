package com.demo.ai.repository;

import com.demo.ai.model.KnowledgeDocument;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends MongoRepository<KnowledgeDocument, String> {

    List<KnowledgeDocument> findByCategory(String category);
}
