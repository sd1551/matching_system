package com.diplom.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "document_comparisons")
@Getter
@Setter
public class DocumentComparison {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "comparison_id", nullable = false)
    private ComparisonResult comparison;

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private DocumentEntity document;

    @Column(name = "graph_image_path", nullable = false)
    private String graphImagePath;

    @Column(name = "comparison_text", columnDefinition = "TEXT", nullable = false)
    private String comparisonText;

    @Column(name = "recommendation", columnDefinition = "TEXT", nullable = false)
    private String recommendation;

    @Column(name = "is_sent")
    private boolean isSent;
}
