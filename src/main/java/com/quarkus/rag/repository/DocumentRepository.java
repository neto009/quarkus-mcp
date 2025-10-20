package com.quarkus.rag.repository;

import com.quarkus.rag.entity.Document;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class DocumentRepository implements PanacheRepository<Document> {

    public List<Document> findUnprocessed() {
        return list("processed = false");
    }

    public List<Document> findByFileName(String fileName) {
        return list("fileName = ?1", fileName);
    }
}

