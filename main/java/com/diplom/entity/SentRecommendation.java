package com.diplom.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sent_recommendations")
@Getter
@Setter
public class SentRecommendation {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "comparison_id", nullable = false)
    private ComparisonResult comparison;

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private DocumentEntity document;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "recommendation", columnDefinition = "TEXT", nullable = false)
    private String recommendation;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "is_read")
    private boolean isRead;
}