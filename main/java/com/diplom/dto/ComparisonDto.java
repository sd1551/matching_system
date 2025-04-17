package com.diplom.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ComparisonDto {
    private UUID vacancyId;
    private String vacancyTitle;
    private List<DocumentForComparisonDto> documents;
}

