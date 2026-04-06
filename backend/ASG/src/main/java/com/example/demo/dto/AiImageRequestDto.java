package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiImageRequestDto {
    private String keyword;
    private String category;
}

//=============================================================================
//AiImageRequestDto.java (내부 클래스 또는 별도 파일)
//역할: Flask /api/ai/generate 엔드포인트로 전송할 요청 데이터를 담는 DTO.
//
//설계 의도:
//- 서비스 레이어가 Flask 인터페이스 명세에 직접 의존하지 않도록
//DTO로 분리하여 추후 Flask API 변경 시 이 클래스만 수정하면 됨.
//=============================================================================