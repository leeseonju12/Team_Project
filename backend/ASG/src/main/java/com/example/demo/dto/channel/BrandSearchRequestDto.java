package com.example.demo.dto.channel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
public class BrandSearchRequestDto {
    private Long brandId;      // DB 식별용
    private String brandName;  // 실제 검색어 (성심당 등)
    private String period;     // "week", "month", "year" (토글 값)
}