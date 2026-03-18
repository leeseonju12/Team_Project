package com.example.demo.controller.auth;

import com.example.demo.security.PrincipalDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthViewController {

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/logout/success")
    public String logoutSuccessPage() {
        return "logout-success";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal PrincipalDetails principal, Model model) {
        model.addAttribute("loginUser", principal.getUser());
        return "dashboard";
    }
    
    @GetMapping("/landing-page")
    public String landing() {
        // -- 확인: templates/landing-page.html 파일을 찾아감 --
        return "landing-page"; 
    }
}
