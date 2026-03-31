package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "fb_comments")
@Getter
@Setter
public class FacebookComment {

    @Id // 🌟 이게 PK(기본키)임을 명시! JPA가 이걸 기준으로 중복을 걸러냅니다.
    private String commentId;
    
    private String postId;
    private String message;
    private String authorName;
    private String createdTime;
}