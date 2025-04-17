package com.diplom.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "comparison_results")
@Getter
@Setter
public class ComparisonResult {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "vacancy_id", nullable = false)
    private VacancyEntity vacancy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @OneToMany(mappedBy = "comparison", cascade = CascadeType.ALL)
    private List<DocumentComparison> documentComparisons = new ArrayList<>();
}
