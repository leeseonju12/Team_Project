package com.example.demo.repository.customerCenter;

import com.example.demo.entity.customerCenter.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaqRepository extends JpaRepository<Faq, Long> {
    List<Faq> findAllByOrderByCreatedAtDesc();
}
