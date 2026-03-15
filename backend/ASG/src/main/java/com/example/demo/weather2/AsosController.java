package com.example.demo.weather2;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/asos")
public class AsosController {

    private final AsosDailyService dailyService;
    private final AsosHourlyService hourlyService;
    private final AsosDataProcessor dataProcessor;

    public AsosController(AsosDailyService dailyService,
                          AsosHourlyService hourlyService,
                          AsosDataProcessor dataProcessor) {
        this.dailyService  = dailyService;
        this.hourlyService = hourlyService;
        this.dataProcessor = dataProcessor;
    }

    /** 일자료 원본 조회 */
    @GetMapping("/daily")
    public ResponseEntity<List<AsosDailyItem>> getDaily(
            @RequestParam String startDt,
            @RequestParam String endDt,
            @RequestParam(defaultValue = "108") String stnIds) {
        return ResponseEntity.ok(dailyService.getDailyData(startDt, endDt, stnIds));
    }

    /** 일자료 가공 조회 (쾌적지수 + 날씨등급 포함) */
    @GetMapping("/daily/processed")
    public ResponseEntity<List<AsosDailyProcessed>> getDailyProcessed(
            @RequestParam String startDt,
            @RequestParam String endDt,
            @RequestParam(defaultValue = "108") String stnIds) {
        List<AsosDailyItem> raw = dailyService.getDailyData(startDt, endDt, stnIds);
        return ResponseEntity.ok(dataProcessor.process(raw));
    }

    /** 시간자료 원본 조회 */
    @GetMapping("/hourly")
    public ResponseEntity<List<AsosHourlyItem>> getHourly(
            @RequestParam String startDt,
            @RequestParam(defaultValue = "00") String startHh,
            @RequestParam String endDt,
            @RequestParam(defaultValue = "23") String endHh,
            @RequestParam(defaultValue = "108") String stnIds) {
        return ResponseEntity.ok(hourlyService.getHourlyData(startDt, startHh, endDt, endHh, stnIds));
    }
}

/*
 * 주소 : http://localhost:8080/asos.html
 * 
asos.html  조건 입력 UI + 테이블 렌더링 
AsosController URL 파라미터 수신 → Service 위임
AsosDailyService 기상청 일자료 API 호출 + 파싱
AsosHourlyService 기상청 시간자료 API 호출 + 파싱
AsosApiResponse<T> 기상청 공통 JSON 구조 매핑
AsosDailyItem, AsosHourlyItem  파싱된 데이터 담는 그릇 
 * 
 */