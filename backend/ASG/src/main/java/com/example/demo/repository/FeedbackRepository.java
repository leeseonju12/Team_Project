package com.example.demo.repository;

import com.example.demo.domain.CustomerFeedback; // 엔티티 경로에 맞게 임포트

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<CustomerFeedback, Long> {
	
	boolean existsByExternalId(String externalId);
	Optional<CustomerFeedback> findByExternalId(String externalId);
}