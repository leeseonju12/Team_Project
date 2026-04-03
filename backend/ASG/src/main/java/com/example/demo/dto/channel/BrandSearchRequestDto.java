package com.example.demo.dto.channel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
public class BrandSearchRequestDto {
    private Long brandId;
    private String brandName;
    private String period;
    private String industryType;

    // Service에서 직접 생성할 때 사용
 // 기존 호환용 (삭제하면 안 됨)
    public BrandSearchRequestDto(String brandName, String period) {
        this.brandName = brandName;
        this.period = period;
    }

    // 브랜드 정보 포함용
    public BrandSearchRequestDto(String brandName, String period, String industryType) {
        this.brandName = brandName;
        this.period = period;
        this.industryType = industryType;
    }
}

/*

채널 성과 분석 페이지에서 쓰이고 있음
마인드맵 차트

*/