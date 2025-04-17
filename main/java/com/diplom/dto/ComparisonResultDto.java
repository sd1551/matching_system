package com.diplom.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ComparisonResultDto {
    private UUID id;
    private UUID vacancyId;
    private String vacancyTitle;
    private LocalDateTime createdAt;
    private String adminNotes;
    private List<DocumentComparisonDto> comparisons;
}

