package com.diplom.repo;

import com.diplom.entity.SentRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SentRecommendationRepository extends JpaRepository<SentRecommendation, UUID> {
    List<SentRecommendation> findByUserId(UUID userId);
    List<SentRecommendation> findByComparisonId(UUID comparisonId);
    List<SentRecommendation> findByDocumentId(UUID documentId);
}
