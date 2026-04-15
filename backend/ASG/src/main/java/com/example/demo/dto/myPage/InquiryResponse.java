package com.example.demo.dto.myPage;

import com.example.demo.entity.myPage.Inquiry;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class InquiryResponse {

    private Long id;
    private String title;
    private String body;
    private String email;
    private String status;
    private LocalDateTime createdAt;

    public InquiryResponse(Inquiry inquiry) {
        this.id        = inquiry.getId();
        this.title     = inquiry.getTitle();
        this.body      = inquiry.getContent();  // content → body (JS 스펙 맞춤)
        this.email     = inquiry.getEmail();
        this.status    = inquiry.getStatus();
        this.createdAt = inquiry.getCreatedAt();
    }
}
