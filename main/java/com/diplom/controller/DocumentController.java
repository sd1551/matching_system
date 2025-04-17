package com.diplom.controller;

import com.diplom.dto.DocumentWithUserDto;
import com.diplom.entity.DocumentEntity;
import com.diplom.entity.Role;
import com.diplom.entity.UserEntity;
import com.diplom.security.JwtTokenProvider;
import com.diplom.service.AuthService;
import com.diplom.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public String documentsPage() {
        return "documents";
    }

    @GetMapping("/my")
    public ResponseEntity<List<DocumentEntity>> getUserDocuments(
            @CookieValue("token") String token,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        UserEntity user = authService.getUserFromToken(token);
        List<DocumentEntity> documents = documentService.getUserDocuments(user.getId(), page, size);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/all")
    public ResponseEntity<List<DocumentWithUserDto>> getAllDocuments(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<DocumentWithUserDto> documents = documentService.getAllDocuments(page, size, username, date);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam("id") UUID documentId,
            @CookieValue(value = "token", required = false) String token) throws IOException {

        // Проверка авторизации
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserEntity user = authService.getUserFromToken(token);
        DocumentEntity document = documentService.getDocumentById(documentId);

        // Проверка прав доступа
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isOwner = document.getUser().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Подготовка файла для скачивания
        Path filePath = Paths.get(document.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Настройка заголовков
        String filename = document.getFileName();
        String contentDisposition = "attachment; filename=\"" + filename + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentLength(resource.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable("id") UUID documentId,
            @CookieValue("token") String token) {
        try {
            UserEntity user = authService.getUserFromToken(token);
            documentService.deleteDocument(documentId, user.getId());
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).build();
        }
    }
}