package com.example.demo.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MypageController {

    @GetMapping("/mypage")
    public String Mypage() {
        return "mypage";
    }

    /*@GetMapping("/mypage/payment")
    public String Payment() {
        return "payment";
    }*/
}