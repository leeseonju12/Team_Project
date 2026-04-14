package com.sosial.damoa.repository;

import com.sosial.damoa.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    List<Reply> findByInquiryIdOrderByIdAsc(Long inquiryId);
    void deleteByInquiryId(Long inquiryId);
}