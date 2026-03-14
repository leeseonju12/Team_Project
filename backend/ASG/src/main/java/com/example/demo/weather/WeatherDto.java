package com.example.demo.weather;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeatherDto {

    private String baseDate;      // 기준 날짜 (yyyyMMdd)
    private String baseTime;      // 기준 시각 (HHmm)

    private String temperature;   // T1H: 기온 (°C)
    private String humidity;      // REH: 습도 (%)
    private String windSpeed;     // WSD: 풍속 (m/s)
    private String windDirection; // VEC: 풍향 (°)
    private String rainfall;      // RN1: 1시간 강수량 (mm)
    private String ptyCode;       // PTY: 강수형태 코드
    private String ptyName;       // PTY: 강수형태 한글명

    private int nx;
    private int ny;
}
