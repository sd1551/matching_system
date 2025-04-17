package com.diplom.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class VacancyDocumentDto {
    private UUID id;
    private String fileName;

    public VacancyDocumentDto(UUID id, String fileName) {
        this.id = id;
        this.fileName = fileName;
    }
}
