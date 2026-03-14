package com.example.demo.APItest;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


@Getter
@NoArgsConstructor
@Entity
@Table(name = "feedback_source")
public class GoogleReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String authorName;

    @Column(nullable = false, length = 30)
    private String platform;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String originalText;

    @Column
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public GoogleReviewEntity(String authorName, String platform, String originalText) {
        this.authorName = authorName;
        this.platform   = platform;
        this.originalText = originalText;
    }
}