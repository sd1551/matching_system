package com.diplom.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class VacancyRequest {
    private String title;
    private String description;
    private Set<String> hardSkills;
    private Set<String> softSkills;
}
