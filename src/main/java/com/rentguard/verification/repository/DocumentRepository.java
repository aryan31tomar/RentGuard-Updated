package com.rentguard.verification.repository;

import com.rentguard.verification.model.Document;
import com.rentguard.verification.model.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByOwnerId(Long ownerId);
    List<Document> findByPropertyId(Long propertyId);
    List<Document> findByStatus(DocumentStatus status);
    long countByStatus(DocumentStatus status);
}
