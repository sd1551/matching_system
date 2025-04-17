package com.diplom.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DocumentComparisonDto {
    private UUID documentId;
    private String documentName;
    private String userName;
    private String graphImagePath;
    private String comparisonText;
    private String recommendation;
    private boolean isSent;
}
