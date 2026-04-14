package com.sosial.damoa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private String email;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    private String status;

    @Column(columnDefinition = "TEXT")
    private String attachmentNames; // 원본 파일명들
    @Column(columnDefinition = "TEXT")
    private String attachmentSavedNames; // 서버 저장 파일명들

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}