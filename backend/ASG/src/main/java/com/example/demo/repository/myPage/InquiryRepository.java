package com.example.demo.repository.myPage;

import com.example.demo.entity.myPage.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findByEmailOrderByCreatedAtDesc(String email);
}
