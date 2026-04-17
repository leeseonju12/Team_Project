package com.example.demo.repository.customerCenter;

import com.example.demo.entity.customerCenter.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    List<Reply> findByInquiryIdOrderByIdAsc(Long inquiryId);
    void deleteByInquiryId(Long inquiryId);
}
