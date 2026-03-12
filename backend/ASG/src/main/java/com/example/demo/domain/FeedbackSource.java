package com.example.demo.domain;

import java.time.LocalDateTime;

import com.example.demo.domain.enums.Platform;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "feedback_source")
@Data
public class FeedbackSource {
	
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "source_id")
    private Long id;

    private String authorName;

    @Enumerated(EnumType.STRING)
    private Platform platform;

    @Column(columnDefinition = "TEXT")
    private String originalText;

    private LocalDateTime createdAt;
    
}
