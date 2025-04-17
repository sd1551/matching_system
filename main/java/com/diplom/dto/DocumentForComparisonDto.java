package com.diplom.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DocumentForComparisonDto {
    private UUID id;
    private String fileName;
    private String userName;
}
