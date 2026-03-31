package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // 이 클래스가 컨트롤러임을 스프링에게 알려줍니다.
public class LandingPageController {

    // 브라우저에 http://localhost:8080/landing-page 를 입력했을 때 실행됩니다.
    @GetMapping("/landing-page")
    public String landingPage() {
        // "landing"이라는 이름의 HTML 파일(Thymeleaf 등)을 찾아서 보여줍니다.
        // 위치: src/main/resources/templates/landing.html
        return "landing-page"; 
    }
}