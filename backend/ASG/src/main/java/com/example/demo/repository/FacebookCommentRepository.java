package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.CustomerFeedback;
import com.example.demo.entity.FacebookComment;

public interface FacebookCommentRepository extends JpaRepository<FacebookComment, String> {
	
}