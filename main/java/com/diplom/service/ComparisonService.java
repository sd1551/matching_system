package com.diplom.service;

import com.diplom.dto.ComparisonDto;
import com.diplom.dto.ComparisonResultDto;
import com.diplom.dto.DocumentComparisonDto;
import com.diplom.dto.DocumentForComparisonDto;
import com.diplom.entity.*;
import com.diplom.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComparisonService {

    private final VacancyRepository vacancyRepository;
    private final ComparisonRepository comparisonRepository;
    private final DocumentComparisonRepository documentComparisonRepository;
    private final SentRecommendationRepository sentRecommendationRepository;
    private final UserRepository userRepository;

    public List<ComparisonDto> getVacanciesWithDocuments() {
        return vacancyRepository.findAll().stream()
                .filter(v -> !v.getDocuments().isEmpty())
                .map(v -> {
                    ComparisonDto dto = new ComparisonDto();
                    dto.setVacancyId(v.getId());
                    dto.setVacancyTitle(v.getTitle());
                    dto.setDocuments(v.getDocuments().stream()
                            .map(d -> {
                                DocumentForComparisonDto docDto = new DocumentForComparisonDto();
                                docDto.setId(d.getId());
                                docDto.setFileName(d.getFileName());
                                docDto.setUserName(d.getUser().getUsername());
                                return docDto;
                            })
                            .collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ComparisonResultDto createComparison(UUID vacancyId, UUID adminId) {
        VacancyEntity vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new RuntimeException("Вакансия не найдена"));

        UserEntity admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Админ не найден"));

        ComparisonResult comparison = new ComparisonResult();
        comparison.setVacancy(vacancy);
        comparison.setCreatedAt(LocalDateTime.now());
        comparison.setAdminNotes("Результаты автоматического сравнения");

        //
        //
        //
        //MOCK
        //
        //
        //

        // Создаем список для documentComparisons
        List<DocumentComparison> comparisons = new ArrayList<>();

        for (DocumentEntity document : vacancy.getDocuments()) {
            DocumentComparison docComparison = new DocumentComparison();
            docComparison.setComparison(comparison);
            docComparison.setDocument(document);
            docComparison.setGraphImagePath("/images/graphs/" + UUID.randomUUID() + ".png");
            docComparison.setComparisonText("Анализ для " + document.getFileName());
            docComparison.setRecommendation("Улучши свои навыки в областях XYZ");
            docComparison.setSent(false);
            comparisons.add(docComparison);
        }

        // Устанавливаем список сравнений
        comparison.setDocumentComparisons(comparisons);

        // Сохраняем
        comparison = comparisonRepository.save(comparison);

        // Сохраняем все documentComparisons
        documentComparisonRepository.saveAll(comparisons);

        return convertToDto(comparison);
    }

    public ComparisonResultDto getComparisonResult(UUID comparisonId) {
        ComparisonResult comparison = comparisonRepository.findById(comparisonId)
                .orElseThrow(() -> new RuntimeException("Сравнение не найдено"));
        return convertToDto(comparison);
    }

    @Transactional
    public void updateRecommendations(UUID comparisonId, List<DocumentComparisonDto> updates) {
        ComparisonResult comparison = comparisonRepository.findById(comparisonId)
                .orElseThrow(() -> new RuntimeException("Сравнение не найдено"));

        for (DocumentComparisonDto update : updates) {
            DocumentComparison docComparison = documentComparisonRepository.findById(update.getDocumentId())
                    .orElseThrow(() -> new RuntimeException("Сравнение документа не обнаружено"));
            docComparison.setRecommendation(update.getRecommendation());
            documentComparisonRepository.save(docComparison);
        }
    }

    @Transactional
    public void sendRecommendation(UUID comparisonId, UUID documentId, String recommendation) {
        try {
            DocumentComparison docComparison = documentComparisonRepository.findByComparisonIdAndDocumentId(comparisonId, documentId)
                    .orElseThrow(() -> new RuntimeException("Сравнение документа не обнаружено"));

            if (docComparison.isSent()) {
                throw new RuntimeException("Рекомендация выслана");
            }

            SentRecommendation sent = new SentRecommendation();
            sent.setComparison(docComparison.getComparison());
            sent.setDocument(docComparison.getDocument());
            sent.setUser(docComparison.getDocument().getUser());
            sent.setRecommendation(recommendation);
            sent.setSentAt(LocalDateTime.now());
            sentRecommendationRepository.save(sent);

            docComparison.setSent(true);
            documentComparisonRepository.save(docComparison);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка отправки рекомендаций", e);
        }
    }

    @Transactional
    public void sendAllRecommendations(UUID comparisonId) {
        List<DocumentComparison> comparisons = documentComparisonRepository.findByComparisonId(comparisonId);

        for (DocumentComparison docComparison : comparisons) {
            if (!docComparison.isSent()) {
                SentRecommendation sent = new SentRecommendation();
                sent.setComparison(docComparison.getComparison());
                sent.setDocument(docComparison.getDocument());
                sent.setUser(docComparison.getDocument().getUser());
                sent.setRecommendation(docComparison.getRecommendation());
                sent.setSentAt(LocalDateTime.now());
                sentRecommendationRepository.save(sent);

                docComparison.setSent(true);
                documentComparisonRepository.save(docComparison);
            }
        }
    }

    private ComparisonResultDto convertToDto(ComparisonResult comparison) {
        ComparisonResultDto dto = new ComparisonResultDto();
        dto.setId(comparison.getId());
        dto.setVacancyId(comparison.getVacancy().getId());
        dto.setVacancyTitle(comparison.getVacancy().getTitle());
        dto.setCreatedAt(comparison.getCreatedAt());
        dto.setAdminNotes(comparison.getAdminNotes());

        // Добавляем проверку на null
        if (comparison.getDocumentComparisons() != null) {
            dto.setComparisons(comparison.getDocumentComparisons().stream()
                    .map(dc -> {
                        DocumentComparisonDto dcDto = new DocumentComparisonDto();
                        dcDto.setDocumentId(dc.getDocument().getId());
                        dcDto.setDocumentName(dc.getDocument().getFileName());
                        dcDto.setUserName(dc.getDocument().getUser().getUsername());
                        dcDto.setGraphImagePath(dc.getGraphImagePath());
                        dcDto.setComparisonText(dc.getComparisonText());
                        dcDto.setRecommendation(dc.getRecommendation());
                        dcDto.setSent(dc.isSent());
                        return dcDto;
                    })
                    .collect(Collectors.toList()));
        } else {
            dto.setComparisons(Collections.emptyList());
        }

        return dto;
    }
}