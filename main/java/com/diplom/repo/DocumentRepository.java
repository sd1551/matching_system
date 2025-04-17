package com.diplom.repo;

import com.diplom.entity.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID>, JpaSpecificationExecutor<DocumentEntity> {
    List<DocumentEntity> findByUserId(UUID userId);
    Page<DocumentEntity> findByUserId(UUID userId, Pageable pageable);
    Optional<DocumentEntity> findByIdAndUserId(UUID id, UUID userId);
}
