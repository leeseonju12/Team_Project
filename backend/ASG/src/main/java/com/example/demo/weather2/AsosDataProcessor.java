package com.example.demo.weather2;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ASOS 일자료 가공 처리기
 *
 * ── 날씨 등급 분류 ──
 *   눈  : 강수량 > 0 AND 평균기온 <= 0℃
 *   비  : 강수량 > 0 AND 평균기온 >  0℃
 *   흐림 : 강수량 = 0 AND 일조시간 < 4hr
 *   맑음 : 강수량 = 0 AND 일조시간 >= 4hr
 *
 * ── 쾌적지수 (0~100) ──
 *   기온 점수  (40점) : 15~25℃ 구간 최고, 벗어날수록 감점
 *   강수 점수  (30점) : 강수 없으면 30점, 있으면 0점
 *   습도 점수  (20점) : 40~60% 구간 최고, 벗어날수록 감점
 *   풍속 점수  (10점) : 0~3m/s 구간 최고, 초과 시 감점
 */
@Component
public class AsosDataProcessor {

    public List<AsosDailyProcessed> process(List<AsosDailyItem> items) {
        return items.stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    private AsosDailyProcessed convert(AsosDailyItem src) {
        AsosDailyProcessed p = new AsosDailyProcessed();

        // 원본 필드 복사
        p.setTm(src.getTm());
        p.setStnNm(src.getStnNm());
        p.setAvgTa(src.getAvgTa());
        p.setMinTa(src.getMinTa());
        p.setMaxTa(src.getMaxTa());
        p.setSumRn(src.getSumRn());
        p.setAvgWs(src.getAvgWs());
        p.setAvgRhm(src.getAvgRhm());
        p.setAvgPa(src.getAvgPa());
        p.setSsDur(src.getSsDur());

        // 수치 파싱
        double avgTa = parse(src.getAvgTa());
        double sumRn = parse(src.getSumRn());
        double avgWs = parse(src.getAvgWs());
        double avgRhm = parse(src.getAvgRhm());
        double ssDur = parse(src.getSsDur());

        // 날씨 등급
        AsosWeatherGrade grade = classifyGrade(avgTa, sumRn, ssDur);
        p.setWeatherGradeLabel(grade.getLabel());
        p.setWeatherGradeColor(grade.getColor());

        // 쾌적지수
        int comfort = calcComfortIndex(avgTa, sumRn, avgRhm, avgWs);
        p.setComfortIndex(comfort);
        p.setComfortLabel(comfortLabel(comfort));

        return p;
    }

    // ── 날씨 등급 분류 ──
    private AsosWeatherGrade classifyGrade(double avgTa, double sumRn, double ssDur) {
        if (sumRn > 0) {
            return avgTa <= 0 ? AsosWeatherGrade.SNOWY : AsosWeatherGrade.RAINY;
        }
        return ssDur >= 4 ? AsosWeatherGrade.SUNNY : AsosWeatherGrade.CLOUDY;
    }

    // ── 쾌적지수 계산 ──
    private int calcComfortIndex(double avgTa, double sumRn, double avgRhm, double avgWs) {

        // 기온 점수 (40점)
        double tempScore;
        if (avgTa >= 15 && avgTa <= 25) {
            tempScore = 40;
        } else if (avgTa >= 10 && avgTa < 15) {
            tempScore = 40 - (15 - avgTa) * 2.5;
        } else if (avgTa > 25 && avgTa <= 32) {
            tempScore = 40 - (avgTa - 25) * 2.5;
        } else {
            tempScore = Math.max(0, 40 - Math.abs(avgTa - 20) * 2);
        }

        // 강수 점수 (30점)
        double rainScore = (sumRn <= 0) ? 30 : 0;

        // 습도 점수 (20점)
        double humScore;
        if (avgRhm >= 40 && avgRhm <= 60) {
            humScore = 20;
        } else if (avgRhm > 60 && avgRhm <= 80) {
            humScore = 20 - (avgRhm - 60) * 0.8;
        } else if (avgRhm < 40 && avgRhm >= 20) {
            humScore = 20 - (40 - avgRhm) * 0.8;
        } else {
            humScore = 0;
        }

        // 풍속 점수 (10점)
        double windScore;
        if (avgWs <= 3) {
            windScore = 10;
        } else if (avgWs <= 7) {
            windScore = 10 - (avgWs - 3) * 1.5;
        } else {
            windScore = 0;
        }

        int total = (int) Math.round(tempScore + rainScore + humScore + windScore);
        return Math.max(0, Math.min(100, total));
    }

    // ── 쾌적 라벨 ──
    private String comfortLabel(int score) {
        if (score >= 80) return "매우 좋음";
        if (score >= 60) return "좋음";
        if (score >= 40) return "보통";
        return "나쁨";
    }

    // ── 안전한 숫자 파싱 ──
    private double parse(String val) {
        if (val == null || val.isBlank()) return 0.0;
        try { return Double.parseDouble(val.trim()); }
        catch (NumberFormatException e) { return 0.0; }
    }
}
