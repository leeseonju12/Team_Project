package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true)); // HTTPS URL 강제
    }

    /**
     * 이미지를 Cloudinary에 업로드하고 외부 접근 가능한 완벽한 HTTPS 주소를 반환합니다.
     */
    public String uploadImage(MultipartFile file) {
        try {
            // 1. Cloudinary 서버로 파일 전송 (빈 Map은 기본 옵션을 의미)
            //Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "format", "jpg"));
            // 2. 인스타그램이 가장 좋아하는 완벽한 직링크(secure_url) 반환
            return uploadResult.get("secure_url").toString();
            
        } catch (IOException e) {
            throw new RuntimeException("Cloudinary 이미지 업로드 실패: " + e.getMessage());
        }
    }
}