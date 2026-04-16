package com.example.demo.controller.customerCenter;

import com.example.demo.entity.customerCenter.AdminUser;
import com.example.demo.repository.customerCenter.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminUserRepository adminUserRepository;

    /**
     * 관리자 로그인
     * POST /api/admin/login
     *
     * [주의] 현재 DB에 평문 비밀번호로 시드 데이터가 입력되어 있으므로
     * PasswordEncoder 없이 평문 비교합니다.
     * 운영 전환 시 BCryptPasswordEncoder로 교체 필요.
     */
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> req) {
        String loginId = req.get("id");
        String password = req.get("pw");

        AdminUser adminUser = adminUserRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."));

        // 평문 비교 (운영 전 BCrypt로 교체 필요)
        if (!password.equals(adminUser.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        return Map.of(
                "result", "ok",
                "token",  "admin-token",
                "name",   adminUser.getName()
        );
    }
}
