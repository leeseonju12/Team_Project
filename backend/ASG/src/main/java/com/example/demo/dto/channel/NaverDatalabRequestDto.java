package com.example.demo.dto.channel;


import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
 
@JsonInclude(JsonInclude.Include.NON_NULL)  // null 필드는 JSON에서 제외
public record NaverDatalabRequestDto(
    String startDate,
    String endDate,
    String timeUnit,
    List<KeywordGroup> keywordGroups,
    String gender,       // "f" | "m" | null(전체)
    List<String> ages    // ["1"]=10대, ["2"]=20대 ... ["6"]=60대+ | null(전체)
) {
    // gender/ages 없는 기본 생성자 (기존 코드 호환)
    public NaverDatalabRequestDto(String startDate, String endDate,
                                  String timeUnit, List<KeywordGroup> keywordGroups) {
        this(startDate, endDate, timeUnit, keywordGroups, null, null);
    }
 
    public record KeywordGroup(
        String groupName,
        List<String> keywords
    ) {}
}
 