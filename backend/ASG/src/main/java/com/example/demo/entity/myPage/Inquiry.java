package com.example.demo.entity.myPage;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry")
@Getter
@Setter
@NoArgsConstructor
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── customerCenter 추가 필드 ──────────────────────────
    @Column(length = 50)
    private String type;                        // 문의 유형 (가입·연동 / 콘텐츠 생성 / 시스템오류 / 계정 / 기타)

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;                        // 문의 본문 (customerCenter 호환용)

    // ── myPage 기존 필드 (InquiryResponse 호환 유지) ──────
    @Column(columnDefinition = "TEXT")
    private String content;                     // 문의 본문 (myPage 기존 호환용)

    @Column(nullable = false, length = 50)
    private String status;                      // 미처리 / 처리중 / 처리완료

    @Column(columnDefinition = "TEXT")
    private String attachmentNames;             // 첨부파일 원본 파일명 (콤마 구분)

    @Column(columnDefinition = "TEXT")
    private String attachmentSavedNames;        // 첨부파일 서버 저장명 (콤마 구분)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}