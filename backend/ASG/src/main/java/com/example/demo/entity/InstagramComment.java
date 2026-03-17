package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.Data;

@Entity
@Data // Lombok: getter, setter, toString 자동 생성
public class InstagramComment {

    @Id
    private String id; // 인스타그램 댓글 ID (Primary Key)

    private String mediaId; // 어떤 게시물(피드)에 달린 댓글인지 기억하기 위함
    private String username; // 작성자 아이디
    
    @Column(length = 1000) // 댓글이 길 수 있으니 넉넉하게
    private String text; // 댓글 내용
    
    private String timestamp; // 작성 시간
    private int likeCount; // 좋아요 수
}