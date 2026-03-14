package com.example.demo.weather;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ForecastDto {

    private String baseDate;
    private String baseTime;
    private int nx;
    private int ny;
    private List<ForecastItemDto> items;

    @Getter
    @Builder
    public static class ForecastItemDto {
        private String fcstDate;    // 예보 날짜 (yyyyMMdd)
        private String fcstTime;    // 예보 시각 (HHmm)
        private String temperature; // T1H(초단기) / TMP(단기): 기온 °C
        private String skyCode;     // SKY: 하늘상태 코드
        private String skyName;     // SKY: 하늘상태 한글
        private String ptyCode;     // PTY: 강수형태 코드
        private String ptyName;     // PTY: 강수형태 한글
        private String humidity;    // REH: 습도 %
        private String windSpeed;   // WSD: 풍속 m/s
        private String rainfall;    // RN1(초단기) / PCP(단기): 강수량
        private String pop;         // POP: 강수확률 % (단기만)
        private String tmn;         // TMN: 일 최저기온 (단기만)
        private String tmx;         // TMX: 일 최고기온 (단기만)
    }
}
