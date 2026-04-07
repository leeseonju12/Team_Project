package com.example.demo.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.HashMap;

// 마이페이지에서 쓰이는 cloudinary

@Configuration
public class CloudinaryConfig {

    @Value("${mypage.cloudinary.cloud-name}")
    private String cloudName;

    @Value("${mypage.cloudinary.api-key}")
    private String apiKey;

    @Value("${mypage.cloudinary.api-secret}")
    private String apiSecret;

    @Bean(name = "mypageCloudinary")
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        return new Cloudinary(config);
    }
}