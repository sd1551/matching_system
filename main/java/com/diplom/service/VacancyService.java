package com.diplom.service;

import com.diplom.dto.VacancyDocumentDto;
import com.diplom.dto.VacancyWithDocumentsDto;
import com.diplom.entity.DocumentEntity;
import com.diplom.entity.Role;
import com.diplom.entity.UserEntity;
import com.diplom.entity.VacancyEntity;
import com.diplom.repo.DocumentRepository;
import com.diplom.repo.VacancyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VacancyService {

    private final VacancyRepository vacancyRepository;
    private final DocumentRepository documentRepository;

    @Value("${skills.hard-skills-file}")
    private String hardSkillsFile;

    @Value("${skills.soft-skills-file}")
    private String softSkillsFile;

    @Transactional
    public VacancyEntity createVacancy(VacancyEntity vacancy, UserEntity createdBy) {
        vacancy.setCreatedBy(createdBy);
        if (vacancy.getHardSkills() == null) vacancy.setHardSkills(new HashSet<>());
        if (vacancy.getSoftSkills() == null) vacancy.setSoftSkills(new HashSet<>());
        return vacancyRepository.save(vacancy);
    }

    public List<VacancyWithDocumentsDto> getUserVacancies(UUID userId) {
        List<VacancyEntity> vacancies = vacancyRepository.findByCreatedById(userId);
        return mapToVacancyWithDocumentsDto(vacancies);
    }

    public List<VacancyWithDocumentsDto> getAllVacancies(UserEntity user) {
        // Все пользователи видят все вакансии
        List<VacancyEntity> vacancies = vacancyRepository.findAll();
        return mapToVacancyWithDocumentsDto(vacancies);
    }

    @Transactional
    public void attachDocument(UUID vacancyId, UUID documentId, UUID userId) {
        VacancyEntity vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new RuntimeException("Вакансии не найдены"));

        DocumentEntity document = documentRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new RuntimeException("Документ не найден или доступ запрещен"));

        // Проверяем, что документ принадлежит пользователю
        if (!document.getUser().getId().equals(userId)) {
            throw new RuntimeException("Вы можете прикрепить только свои собственные документы");
        }

        vacancy.getDocuments().add(document);
        vacancyRepository.save(vacancy);
    }

    @Transactional
    public void detachDocument(UUID vacancyId, UUID documentId, UUID userId) {
        VacancyEntity vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new RuntimeException("Вакансия не найдена"));

        // Проверяем, что документ принадлежит пользователю или пользователь создал вакансию
        boolean isVacancyOwner = vacancy.getCreatedBy().getId().equals(userId);
        boolean isDocumentOwner = vacancy.getDocuments().stream()
                .anyMatch(doc -> doc.getId().equals(documentId) && doc.getUser().getId().equals(userId));

        if (!isVacancyOwner && !isDocumentOwner) {
            throw new RuntimeException("У вас нет прав для прикрепления документа");
        }

        vacancy.getDocuments().removeIf(d -> d.getId().equals(documentId));
        vacancyRepository.save(vacancy);
    }

    @Transactional
    public void deleteVacancy(UUID id, UserEntity user) {
        VacancyEntity vacancy = vacancyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Вакансия не найдена"));

        if (user.getRole() != Role.ADMIN && !vacancy.getCreatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("Вы можете удалять только свои собственные вакансии");
        }

        vacancyRepository.delete(vacancy);
    }

    private List<VacancyWithDocumentsDto> mapToVacancyWithDocumentsDto(List<VacancyEntity> vacancies) {
        return vacancies.stream().map(vacancy -> {
            VacancyWithDocumentsDto dto = new VacancyWithDocumentsDto();
            dto.setId(vacancy.getId());
            dto.setTitle(vacancy.getTitle());
            dto.setDescription(vacancy.getDescription());
            dto.setCreatedAt(vacancy.getCreatedAt());
            dto.setCreatedBy(vacancy.getCreatedBy().getUsername());

            List<VacancyDocumentDto> documents = vacancy.getDocuments().stream()
                    .map(doc -> new VacancyDocumentDto(doc.getId(), doc.getFileName()))
                    .collect(Collectors.toList());

            dto.setDocuments(documents);
            dto.setHardSkills(vacancy.getHardSkills());
            dto.setSoftSkills(vacancy.getSoftSkills());

            return dto;
        }).collect(Collectors.toList());
    }

    public List<String> searchHardSkills(String query) throws IOException {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return searchSkills(query, hardSkillsFile);
    }

    public List<String> searchSoftSkills(String query) throws IOException {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return searchSkills(query, softSkillsFile);
    }

    private List<String> searchSkills(String query, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return Collections.emptyList();
        }

        return Files.lines(path)
                .filter(line -> line.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void addHardSkill(String skill) throws IOException {
        addSkill(skill, hardSkillsFile);
    }

    public void addSoftSkill(String skill) throws IOException {
        addSkill(skill, softSkillsFile);
    }

    private void addSkill(String skill, String filePath) throws IOException {
        if (!StringUtils.hasText(skill)) {
            throw new IllegalArgumentException("Поле навыков не может быть пустым");
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        }

        boolean exists = Files.lines(path)
                .anyMatch(line -> line.equalsIgnoreCase(skill));

        if (!exists) {
            Files.write(path, (skill + System.lineSeparator()).getBytes(),
                    StandardOpenOption.APPEND);
        }
    }
}