package com.example.demo.domain;

import java.time.LocalDateTime;

import com.example.demo.domain.enums.Platform;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feedback_source")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    
    @Column(unique = true, name = "external_id")
    private String externalId; // 인스타그램 댓글 고유 id
    
}
