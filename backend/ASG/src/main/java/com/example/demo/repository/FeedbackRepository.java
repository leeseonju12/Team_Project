package com.example.demo.repository;

import com.example.demo.domain.CustomerFeedback; // 엔티티 경로에 맞게 임포트
import com.example.demo.domain.enums.AiStatus;
import com.example.demo.domain.enums.FeedbackStatus;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<CustomerFeedback, Long>, JpaSpecificationExecutor<CustomerFeedback> {
	
	boolean existsByExternalId(String externalId);
	Optional<CustomerFeedback> findByExternalId(String externalId);
	
	// DB에 먼저 들어온 순서(ID 오름차순)로 가져오기
    List<CustomerFeedback> findAllByOrderByIdAsc();
    
    // 또는 최근에 들어온 순서(ID 내림차순)로 가져오기
    // List<CustomerFeedback> findAllByOrderByIdDesc();
    
    long countByStatus(FeedbackStatus status);
    long countByAiStatusAndStatusNot(AiStatus aiStatus, FeedbackStatus status);
}
