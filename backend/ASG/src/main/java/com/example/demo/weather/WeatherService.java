package com.example.demo.weather;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.demo.weather.ForecastDto.ForecastItemDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
 
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {
 
    private static final String NCST_URL =
            "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst";
    private static final String ULTRA_FCST_URL =
            "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst";
    private static final String VILAGE_FCST_URL =
            "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
 
    @Value("${weather.api.key}")
    private String serviceKey;
 
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
 
    // 1. 초단기실황
    public WeatherDto getCurrentWeather(int nx, int ny) {
        String baseDate = today();
        String baseTime = ncstBaseTime();
        log.info("초단기실황 호출 - {}/{} nx={} ny={}", baseDate, baseTime, nx, ny);
        String raw = call(NCST_URL, baseDate, baseTime, nx, ny, 10);
        return parseNcst(raw, baseDate, baseTime, nx, ny);
    }
 
    // 2. 초단기예보 (6시간)
    public ForecastDto getUltraShortForecast(int nx, int ny) {
        String baseDate = today();
        String baseTime = ultraFcstBaseTime();
        log.info("초단기예보 호출 - {}/{} nx={} ny={}", baseDate, baseTime, nx, ny);
        String raw = call(ULTRA_FCST_URL, baseDate, baseTime, nx, ny, 60);
        return parseForecast(raw, baseDate, baseTime, nx, ny, false);
    }
 
    // 3. 단기예보 (3일)
    public ForecastDto getShortForecast(int nx, int ny) {
        String baseDate = today();
        String baseTime = vilageFcstBaseTime();
        log.info("단기예보 호출 - {}/{} nx={} ny={}", baseDate, baseTime, nx, ny);
        String raw = call(VILAGE_FCST_URL, baseDate, baseTime, nx, ny, 1000);
        return parseForecast(raw, baseDate, baseTime, nx, ny, true);
    }
 
    // API 호출 공통
    private String call(String apiUrl, String baseDate, String baseTime,
                        int nx, int ny, int numOfRows) {
        String url = apiUrl
                + "?serviceKey=" + serviceKey
                + "&pageNo=1"
                + "&numOfRows=" + numOfRows
                + "&dataType=JSON"
                + "&base_date=" + baseDate
                + "&base_time=" + baseTime
                + "&nx=" + nx
                + "&ny=" + ny;
        try {
            return restTemplate.getForObject(new URI(url), String.class);
        } catch (URISyntaxException e) {
            throw new RuntimeException("URI 생성 실패: " + e.getMessage(), e);
        }
    }
 
    // 파싱 - 초단기실황
    private WeatherDto parseNcst(String rawJson, String baseDate,
                                  String baseTime, int nx, int ny) {
        try {
            WeatherApiResponse api = objectMapper.readValue(rawJson, WeatherApiResponse.class);
            checkResultCode(api);
            Map<String, String> data = api.getResponse().getBody().getItems().getItem()
                    .stream()
                    .collect(Collectors.toMap(
                            WeatherApiResponse.Item::getCategory,
                            WeatherApiResponse.Item::getObsrValue
                    ));
            String ptyCode = data.getOrDefault("PTY", "0");
            return WeatherDto.builder()
                    .baseDate(baseDate).baseTime(baseTime)
                    .temperature(data.get("T1H"))
                    .humidity(data.get("REH"))
                    .windSpeed(data.get("WSD"))
                    .windDirection(data.get("VEC"))
                    .rainfall(data.get("RN1"))
                    .ptyCode(ptyCode)
                    .ptyName(ptyName(ptyCode))
                    .nx(nx).ny(ny)
                    .build();
        } catch (Exception e) {
            log.error("초단기실황 파싱 실패", e);
            throw new RuntimeException("날씨 데이터 파싱 오류", e);
        }
    }
 
    // 파싱 - 예보 공통
    private ForecastDto parseForecast(String rawJson, String baseDate,
                                       String baseTime, int nx, int ny,
                                       boolean isVilage) {
        try {
            WeatherApiResponse api = objectMapper.readValue(rawJson, WeatherApiResponse.class);
            checkResultCode(api);
 
            List<WeatherApiResponse.Item> items =
                    api.getResponse().getBody().getItems().getItem();
 
            // fcstDate+fcstTime 기준으로 그룹핑 (순서 유지)
            Map<String, Map<String, String>> grouped = new LinkedHashMap<>();
            for (WeatherApiResponse.Item item : items) {
                String key = item.getFcstDate() + item.getFcstTime();
                grouped.computeIfAbsent(key, k -> new HashMap<>())
                       .put(item.getCategory(), item.getFcstValue());
            }
 
            List<ForecastItemDto> result = new ArrayList<>();
            for (Map.Entry<String, Map<String, String>> entry : grouped.entrySet()) {
                String key = entry.getKey();
                Map<String, String> d = entry.getValue();
                String fcstDate = key.substring(0, 8);
                String fcstTime = key.substring(8);
                String skyCode  = d.getOrDefault("SKY", "1");
                String ptyCode  = d.getOrDefault("PTY", "0");
 
                result.add(ForecastItemDto.builder()
                        .fcstDate(fcstDate)
                        .fcstTime(fcstTime)
                        .temperature(isVilage ? d.get("TMP") : d.get("T1H"))
                        .skyCode(skyCode)
                        .skyName(skyName(skyCode))
                        .ptyCode(ptyCode)
                        .ptyName(ptyName(ptyCode))
                        .humidity(d.get("REH"))
                        .windSpeed(d.get("WSD"))
                        .rainfall(isVilage ? d.get("PCP") : d.get("RN1"))
                        .pop(d.get("POP"))
                        .tmn(d.get("TMN"))
                        .tmx(d.get("TMX"))
                        .build());
            }
 
            return ForecastDto.builder()
                    .baseDate(baseDate).baseTime(baseTime)
                    .nx(nx).ny(ny).items(result)
                    .build();
        } catch (Exception e) {
            log.error("예보 파싱 실패", e);
            throw new RuntimeException("예보 데이터 파싱 오류", e);
        }
    }
 
    // 기준 시각 계산
    private String ncstBaseTime() {
        LocalTime now = LocalTime.now();
        if (now.getMinute() < 40) now = now.minusHours(1);
        return now.format(DateTimeFormatter.ofPattern("HH")) + "00";
    }
 
    private String ultraFcstBaseTime() {
        LocalTime now = LocalTime.now();
        if (now.getMinute() < 30) now = now.minusHours(1);
        return now.format(DateTimeFormatter.ofPattern("HH")) + "00";
    }
 
    private String vilageFcstBaseTime() {
        int[] baseTimes = {2, 5, 8, 11, 14, 17, 20, 23};
        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();
        int selected = baseTimes[0];
        for (int bt : baseTimes) {
            if (hour > bt || (hour == bt && minute >= 10)) {
                selected = bt;
            }
        }
        return String.format("%02d00", selected);
    }
 
    private String today() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
 
    private void checkResultCode(WeatherApiResponse api) {
        String code = api.getResponse().getHeader().getResultCode();
        if (!"00".equals(code)) {
            String msg = api.getResponse().getHeader().getResultMsg();
            throw new IllegalStateException("API 오류: [" + code + "] " + msg);
        }
    }
 
    private String ptyName(String code) {
        return switch (code) {
            case "0" -> "없음";
            case "1" -> "비";
            case "2" -> "비/눈";
            case "3" -> "눈";
            case "5" -> "빗방울";
            case "6" -> "빗방울·눈날림";
            case "7" -> "눈날림";
            default  -> "알 수 없음";
        };
    }
 
    private String skyName(String code) {
        return switch (code) {
            case "1" -> "맑음";
            case "3" -> "구름많음";
            case "4" -> "흐림";
            default  -> "-";
        };
    }
}