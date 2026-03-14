package com.example.demo.APItest;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GoogleReviewRepository extends JpaRepository<GoogleReviewEntity, Long> {
}