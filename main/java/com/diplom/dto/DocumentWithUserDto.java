package com.diplom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class DocumentWithUserDto {
    private UUID id;
    private String fileName;
    private LocalDateTime uploadDate;
    private String username;
}
