package com.example.demo.weather;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
 
@Slf4j
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {
 
    private final WeatherService weatherService;
 
    /** GET /api/weather/current?nx=60&ny=127 — 초단기실황 */
    @GetMapping("/current")
    public ResponseEntity<WeatherDto> getCurrentWeather(
            @RequestParam(defaultValue = "60") int nx,
            @RequestParam(defaultValue = "127") int ny) {
        return ResponseEntity.ok(weatherService.getCurrentWeather(nx, ny));
    }
 
    /** GET /api/weather/ultra?nx=60&ny=127 — 초단기예보 (6시간) */
    @GetMapping("/ultra")
    public ResponseEntity<ForecastDto> getUltraForecast(
            @RequestParam(defaultValue = "60") int nx,
            @RequestParam(defaultValue = "127") int ny) {
        return ResponseEntity.ok(weatherService.getUltraShortForecast(nx, ny));
    }
 
    /** GET /api/weather/short?nx=60&ny=127 — 단기예보 (3일) */
    @GetMapping("/short")
    public ResponseEntity<ForecastDto> getShortForecast(
            @RequestParam(defaultValue = "60") int nx,
            @RequestParam(defaultValue = "127") int ny) {
        return ResponseEntity.ok(weatherService.getShortForecast(nx, ny));
    }
}

/*

GET /api/weather/current?nx=60&ny=127
접속 주소 : http://localhost:8080/weather.html
@param nx 예보 X 격자 (기본값: 서울 60)
@param ny 예보 Y 격자 (기본값: 서울 127)
     
 브라우저가 weather.html을 열면 JS가 자동으로 /api/weather/current 를 호출
WeatherController가 요청을 받아 WeatherService 호출
WeatherService가 기상청 API를 직접 호출 (서버→서버 통신, 브라우저는 API 키 모름)
기상청 응답 JSON을 파싱해서 WeatherDto로 변환 후 반환
브라우저 JS가 받은 JSON을 화면에 렌더링

날씨정보는 DB에 저장하지 않지만

DB는 켜둬야 + gather 데이터베이스가 존재해 실행이 됩니다
 */