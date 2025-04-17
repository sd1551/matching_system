package com.diplom.service;

import com.diplom.entity.DocumentEntity;
import com.diplom.entity.UserEntity;
import com.diplom.repo.DocumentRepository;
import com.diplom.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class UploadService {

    private static final Logger log = LoggerFactory.getLogger(UploadService.class);
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public UploadService(DocumentRepository documentRepository, UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    public String uploadDocument(UUID userId, MultipartFile file) throws IOException {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String uniqueFileName = UUID.randomUUID() + "_" + fileName;
        Path targetPath = uploadPath.resolve(uniqueFileName);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        DocumentEntity document = new DocumentEntity();
        document.setUser(user);
        document.setFileName(fileName);
        document.setFilePath(targetPath.toString());
        documentRepository.save(document);

        log.info("Файл загружен для пользователя {}: {}", userId, targetPath);
        return targetPath.toString();
    }
}

