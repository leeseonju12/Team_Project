package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper; // 🌟 이거 필수!

@Service
public class ImgbbService {

    @Value("${imgbb.api.key}")
    private String apiKey;

    public String uploadImage(MultipartFile file) {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper(); // 🌟 문자열을 JSON으로 까주는 만능 도구
        
        String url = "https://api.imgbb.com/1/upload?key=" + apiKey;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", file.getResource());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 🌟 1. 에러가 나든 말든 일단 무조건 통째로 문자열(String)로 다 받습니다. (가장 안전)
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            // 🌟 2. 받아온 문자열을 ObjectMapper한테 던져서 JSON 구조로 분석하라고 시킵니다.
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            // 3. 예쁘게 파싱된 데이터에서 url만 쏙 빼서 돌려줍니다.
            return rootNode.get("data").get("url").asText();

        } catch (Exception e) {
            // 만약 여기서 에러가 나면 e.getMessage()에 원본 에러 원인이 텍스트로 다 찍힙니다!
            throw new RuntimeException("ImgBB 업로드 중 오류 발생: " + e.getMessage());
        }
    }
}