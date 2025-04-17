package com.diplom.repo;

import com.diplom.entity.DocumentComparison;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentComparisonRepository extends JpaRepository<DocumentComparison, UUID> {
    Optional<DocumentComparison> findByComparisonIdAndDocumentId(UUID comparisonId, UUID documentId);
    List<DocumentComparison> findByComparisonId(UUID comparisonId);
}