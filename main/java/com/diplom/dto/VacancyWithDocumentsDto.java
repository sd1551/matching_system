package com.diplom.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class VacancyWithDocumentsDto {
    private UUID id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private String createdBy;
    private List<VacancyDocumentDto> documents;
    private Set<String> hardSkills;
    private Set<String> softSkills;
}

