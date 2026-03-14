package com.example.demo.googleReview;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class OutscraperController {

    private final OutscraperService outscraperService;
    
    // 요청 주소 : http://localhost:8080/reviews/scrape
    @PostMapping("/scrape")
    public ResponseEntity<String> scrape() {
        try {
        	// Outscraper 가 query 의 내용을 구글에 검색함
            String query = "메가MGC커피 서울역점";
            outscraperService.scrapeAndSave(query, 100);
            return ResponseEntity.ok("저장 완료");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("실패: " + e.getMessage());
        }
    }
}


/*

Spring Boot 시작 후

http://localhost:8080/reviews/scrape 호출 시 실행
Outscraper API에 Query 에 작성된 가게의 리뷰 100개 요청
응답 파싱 → author_title, review_text 추출
feedback_source 테이블에 INSERT
콘솔에 저장 완료: N건 출력 후 종료


실행 전 체크리스트

application.yml의 DB 정보 (url, username, password) 실제 값으로 변경했는지
outscraper.api-key 실제 API 키 입력했는지
feedback_source 테이블이 DB에 이미 생성되어 있는지
(ddl-auto: none 이므로 자동 생성 안 됨)

  */