package com.diplom.repo;

import com.diplom.entity.VacancyEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VacancyRepository extends JpaRepository<VacancyEntity, UUID> {
    List<VacancyEntity> findByCreatedById(UUID userId);

    @EntityGraph(attributePaths = {"documents", "documents.user", "createdBy"})
    @Query("SELECT v FROM VacancyEntity v WHERE SIZE(v.documents) > 0")
    List<VacancyEntity> findAllWithDocuments();

    @EntityGraph(attributePaths = {"documents", "documents.user", "createdBy"})
    @Query("SELECT v FROM VacancyEntity v WHERE v.id = :id")
    Optional<VacancyEntity> findByIdWithDocuments(UUID id);
}
