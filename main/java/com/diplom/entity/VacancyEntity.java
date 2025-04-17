package com.diplom.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "vacancies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VacancyEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;

    @ElementCollection
    @CollectionTable(name = "vacancy_hard_skills", joinColumns = @JoinColumn(name = "vacancy_id"))
    @Column(name = "skill")
    private Set<String> hardSkills = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "vacancy_soft_skills", joinColumns = @JoinColumn(name = "vacancy_id"))
    @Column(name = "skill")
    private Set<String> softSkills = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "vacancy_documents",
            joinColumns = @JoinColumn(name = "vacancy_id"),
            inverseJoinColumns = @JoinColumn(name = "document_id")
    )
    private Set<DocumentEntity> documents = new HashSet<>();
}
