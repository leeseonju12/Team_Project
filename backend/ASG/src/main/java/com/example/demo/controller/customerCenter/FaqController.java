package com.example.demo.controller.customerCenter;

import com.example.demo.entity.customerCenter.Faq;
import com.example.demo.repository.customerCenter.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FaqController {

    private final FaqRepository faqRepository;

    // 공개 조회 (고객센터 화면)
    @GetMapping("/api/public/faqs")
    public List<Faq> getPublicFaqs() {
        return faqRepository.findAllByOrderByCreatedAtDesc();
    }

    // 관리자 조회
    @GetMapping("/api/admin/faqs")
    public List<Faq> getAdminFaqs() {
        return faqRepository.findAllByOrderByCreatedAtDesc();
    }

    // 관리자 등록
    @PostMapping("/api/admin/faqs")
    public Faq createFaq(@RequestBody Map<String, String> body) {
        String category = body.getOrDefault("category", "").trim();
        String question = body.getOrDefault("question", "").trim();
        String answer   = body.getOrDefault("answer", "").trim();

        if (category.isBlank()) throw new RuntimeException("FAQ 카테고리를 입력하세요");
        if (question.isBlank()) throw new RuntimeException("질문을 입력하세요");
        if (answer.isBlank())   throw new RuntimeException("답변을 입력하세요");

        Faq faq = new Faq();
        faq.setCategory(category);
        faq.setQuestion(question);
        faq.setAnswer(answer);

        return faqRepository.save(faq);
    }

    // 관리자 수정
    @PutMapping("/api/admin/faqs/{id}")
    public Faq updateFaq(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ 없음"));

        String category = body.getOrDefault("category", "").trim();
        String question = body.getOrDefault("question", "").trim();
        String answer   = body.getOrDefault("answer", "").trim();

        if (category.isBlank()) throw new RuntimeException("FAQ 카테고리를 입력하세요");
        if (question.isBlank()) throw new RuntimeException("질문을 입력하세요");
        if (answer.isBlank())   throw new RuntimeException("답변을 입력하세요");

        faq.setCategory(category);
        faq.setQuestion(question);
        faq.setAnswer(answer);

        return faqRepository.save(faq);
    }

    // 관리자 삭제
    @DeleteMapping("/api/admin/faqs/{id}")
    public Map<String, String> deleteFaq(@PathVariable Long id) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ 없음"));

        faqRepository.delete(faq);
        return Map.of("result", "ok");
    }
}
