package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiImageResponseDto {
	@JsonProperty("imageUrl") // Flask의 key값과 정확히 일치해야 함
    private String imageUrl;
    
    @JsonProperty("refinedPrompt")
    private String refinedPrompt;
    
}

//=============================================================================
//AiImageResponseDto.java (내부 클래스 또는 별도 파일)
//역할: Flask로부터 받은 이미지 URL과 실제 사용된 프롬프트를 담는 DTO.
//
//설계 의도:
//- "imageUrl"과 "refinedPrompt" 두 필드만 노출하여
//Flask의 내부 처리 과정(번역, 프롬프트 조립 등)을 Spring Boot에 숨김.
//- refinedPrompt는 관리자 화면이나 디버깅 로그에서 활용 가능.
//=============================================================================