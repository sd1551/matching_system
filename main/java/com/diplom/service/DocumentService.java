package com.diplom.service;

import com.diplom.dto.DocumentWithUserDto;
import com.diplom.entity.DocumentEntity;
import com.diplom.entity.UserEntity;
import com.diplom.repo.DocumentRepository;
import com.diplom.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public List<DocumentEntity> getUserDocuments(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("uploadDate").descending());
        return documentRepository.findByUserId(userId, pageable).getContent();
    }

    public List<DocumentWithUserDto> getAllDocuments(int page, int size, String username, LocalDate date) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("uploadDate").descending());
        Specification<DocumentEntity> spec = Specification.where(null);

        if (username != null && !username.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("user").get("username")), "%" + username.toLowerCase() + "%"));
        }

        if (date != null) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            spec = spec.and((root, query, cb) ->
                    cb.between(root.get("uploadDate"), startOfDay, endOfDay));
        }

        return documentRepository.findAll(spec, pageable)
                .map(doc -> new DocumentWithUserDto(
                        doc.getId(),
                        doc.getFileName(),
                        doc.getUploadDate(),
                        doc.getUser().getUsername()))
                .getContent();
    }

    public void deleteDocument(UUID documentId, UUID userId) {
        DocumentEntity document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Документ не найден"));

        UserEntity currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Проверка прав владельца
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");
        boolean isOwner = document.getUser().getId().equals(userId);

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("У вас нет прав для удаления этого документа");
        }

        try {
            Files.deleteIfExists(Paths.get(document.getFilePath()));
            documentRepository.delete(document);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при попытке удалить файл", e);
        }
    }

    public DocumentEntity getDocumentById(UUID documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Документ не найден"));
    }
}
