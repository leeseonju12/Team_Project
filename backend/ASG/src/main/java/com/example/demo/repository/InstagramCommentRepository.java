package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.InstagramComment;

public interface InstagramCommentRepository extends JpaRepository<InstagramComment, String> {
    // 필요하다면 나중에 "특정 게시물(mediaId)의 댓글만 전부 가져와" 같은 기능도 여기에 한 줄로 추가할 수 있습니다.
    // List<InstagramComment> findByMediaId(String mediaId);
}