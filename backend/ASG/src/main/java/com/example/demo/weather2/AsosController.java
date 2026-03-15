package com.example.demo.weather2;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/asos")
public class AsosController {

    private final AsosDailyService dailyService;
    private final AsosHourlyService hourlyService;

    public AsosController(AsosDailyService dailyService, AsosHourlyService hourlyService) {
        this.dailyService = dailyService;
        this.hourlyService = hourlyService;
    }

    @GetMapping("/daily")
    public ResponseEntity<List<AsosDailyItem>> getDaily(
            @RequestParam String startDt,
            @RequestParam String endDt,
            @RequestParam(defaultValue = "108") String stnIds) {
        List<AsosDailyItem> result = dailyService.getDailyData(startDt, endDt, stnIds);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/hourly")
    public ResponseEntity<List<AsosHourlyItem>> getHourly(
            @RequestParam String startDt,
            @RequestParam(defaultValue = "00") String startHh,
            @RequestParam String endDt,
            @RequestParam(defaultValue = "23") String endHh,
            @RequestParam(defaultValue = "108") String stnIds) {
        List<AsosHourlyItem> result = hourlyService.getHourlyData(startDt, startHh, endDt, endHh, stnIds);
        return ResponseEntity.ok(result);
    }

}  // ← 클래스 닫는 중괄호

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