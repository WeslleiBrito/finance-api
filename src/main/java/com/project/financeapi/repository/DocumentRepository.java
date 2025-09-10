package com.project.financeapi.repository;

import com.project.financeapi.entity.Document;
import com.project.financeapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, String> {
    List<Document> findByCreatedBy(User user);

    Optional<Document> findByIdAndCreatedBy(String id, User user);
}
