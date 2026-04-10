package com.example.demo.entity.myPage;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "content_post")
@Getter @Setter
@NoArgsConstructor
public class ContentPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_platform_id", nullable = false)
    private BrandPlatform brandPlatform;

    @Column(name = "post_title", length = 200)
    private String postTitle;

    @Column(name = "post_type", length = 50)
    private String postType;

    @Column(name = "post_body", columnDefinition = "TEXT")
    private String postBody;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "status", length = 30, nullable = false)
    private String status = "published";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.publishedAt == null) this.publishedAt = LocalDateTime.now();
    }
}