package com.sosial.damoa.repository;

import com.sosial.damoa.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    List<Inquiry> findByEmailOrderByIdDesc(String email);

    List<Inquiry> findAllByOrderByIdDesc();
}