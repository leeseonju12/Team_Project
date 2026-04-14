package com.sosial.damoa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "redirect:/customer-center.html";
    }

    @GetMapping("/inquiry")
    public String inquiryPage() {
        return "redirect:/customer-center.html";
    }

    @GetMapping("/customer-center")
    public String customerCenterPage() {
        return "redirect:/customer-center.html";
    }
}