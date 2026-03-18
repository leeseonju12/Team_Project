package com.example.demo.service;

import com.example.demo.dto.channel.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NaverSearchService {

    private static final Logger log = LoggerFactory.getLogger(NaverSearchService.class);

    private final NaverDatalabClient datalabClient;
    private final JdbcTemplate jdbcTemplate;

    private static final ExecutorService POOL = Executors.newFixedThreadPool(20);
    private static final Semaphore NAVER_SEMAPHORE = new Semaphore(1);

    private static final List<String> AGE_CODES  = List.of("1", "2", "3", "4", "5", "6");
    private static final List<String> AGE_LABELS = List.of("10대", "20대", "30대", "40대", "50대", "60대+");

    @Cacheable(value = "naverDashboard", key = "#brandId + '_' + #period")
    public NaverSearchResponseDto getDashboard(Long brandId, LocalDate from, LocalDate to, String period) {

        // ── 0. DB 및 기본 설정 ───────────────────────────
        Map<String, Object> brand = jdbcTemplate.queryForMap(
                "SELECT brand_name, service_name, industry_type, location_name FROM brand WHERE brand_id = ?", brandId);
        String storeName   = getStoreName(brand);
        List<String> industryKws = getIndustryKeywords(brand);

        String timeUnit = switch (period) {
            case "week"  -> "date";
            case "year"  -> "month";
            default      -> "week";
        };
        String fromStr      = from.toString();
        String toStr        = to.toString();
        String prevFromStr  = getPrevFrom(from, to);
        String prevToStr    = from.minusDays(1).toString();

        // ── 1. 병렬 API 호출 ─────────────────────
        var fTrend = CompletableFuture.supplyAsync(() -> searchWithSemaphore(
                new NaverDatalabRequestDto(fromStr, toStr, timeUnit,
                        List.of(new NaverDatalabRequestDto.KeywordGroup(storeName, List.of(storeName))))), POOL);

        var fTrendPrev = CompletableFuture.supplyAsync(() -> searchWithSemaphore(
                new NaverDatalabRequestDto(prevFromStr, prevToStr, timeUnit,
                        List.of(new NaverDatalabRequestDto.KeywordGroup(storeName, List.of(storeName))))), POOL);

        var fKeywords = CompletableFuture.supplyAsync(() -> searchWithSemaphore(
                new NaverDatalabRequestDto(fromStr, toStr, timeUnit,
                        industryKws.stream().map(kw -> new NaverDatalabRequestDto.KeywordGroup(kw, List.of(kw))).toList())), POOL);

        var fFemaleCur  = CompletableFuture.supplyAsync(() -> getRatioAvg(fromStr, toStr, storeName, "f", null), POOL);
        var fMaleCur    = CompletableFuture.supplyAsync(() -> getRatioAvg(fromStr, toStr, storeName, "m", null), POOL);
        var fFemalePrev = CompletableFuture.supplyAsync(() -> getRatioAvg(prevFromStr, prevToStr, storeName, "f", null), POOL);
        var fMalePrev   = CompletableFuture.supplyAsync(() -> getRatioAvg(prevFromStr, prevToStr, storeName, "m", null), POOL);

        List<CompletableFuture<Double>> fAgeCur = AGE_CODES.stream()
                .map(age -> CompletableFuture.supplyAsync(() -> getRatioAvg(fromStr, toStr, storeName, null, List.of(age)), POOL)).toList();

        List<CompletableFuture<Double>> fAgePrev = AGE_CODES.stream()
                .map(age -> CompletableFuture.supplyAsync(() -> getRatioAvg(prevFromStr, prevToStr, storeName, null, List.of(age)), POOL)).toList();

        List<CompletableFuture<?>> allFutures = new ArrayList<>(List.of(fTrend, fTrendPrev, fKeywords, fFemaleCur, fMaleCur, fFemalePrev, fMalePrev));
        allFutures.addAll(fAgeCur);
        allFutures.addAll(fAgePrev);

        try {
            CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).get();
        } catch (Exception e) {
            log.error("[NaverSearch] 데이터 수신 중 오류: {}", e.getMessage());
        }

        // ── 2. 결과 가공 ──────────────────────────────────────────────────────────

        // [수정] 검색 수 추이를 % 비중으로 변환 (변수명 충돌 방지를 위해 trendList로 명명)
        List<NaverSearchTrendDto> trendList = new ArrayList<>();
        int summaryTotalPercent = 0; 

        try {
            var trendRawData = fTrend.get().results().get(0).data();
            
            // [수정] 해당 기간 모든 지수의 총합 계산 (분모)
            double trendTotalSum = trendRawData.stream().mapToDouble(p -> p.ratio()).sum();

            if (trendTotalSum > 0) {
                int trendRunningSum = 0;
                for (int i = 0; i < trendRawData.size(); i++) {
                    var p = trendRawData.get(i);
                    int percent;
                    
                    if (i < trendRawData.size() - 1) {
                        // [수정] 일반 항목: (개별 지수 / 전체 합계) * 100
                        percent = (int) Math.round((p.ratio() / trendTotalSum) * 100);
                        trendRunningSum += percent;
                    } else {
                        // [수정] 마지막 항목: 반올림 오차 보정 (합계 100% 맞춤)
                        percent = Math.max(0, 100 - trendRunningSum);
                    }

                    trendList.add(NaverSearchTrendDto.builder()
                            .label(formatLabel(p.period(), period))
                            .searchCount(percent) // 이제 0~100 사이의 % 값이 저장됨
                            .build());
                }
                summaryTotalPercent = 100; // 요약 지표도 100% 기준으로 설정
            }
        } catch (Exception e) {
            log.warn("[NaverSearch] 트렌드 데이터 가공 실패: {}", e.getMessage());
        }

        // [기존 유지] 키워드 순위 가공
        List<NaverKeywordDto> keywords = new ArrayList<>();
        try {
            var kwResults = fKeywords.get().results();
            for (int i = 0; i < kwResults.size(); i++) {
                var r = kwResults.get(i);
                int avg = (int) Math.round(r.data().stream().mapToDouble(d -> d.ratio()).average().orElse(0) * 100);
                keywords.add(NaverKeywordDto.builder().keywordText(r.title()).searchCount(avg).rankNo(i + 1).build());
            }
            keywords.sort((a, b) -> b.searchCount() - a.searchCount());
        } catch (Exception e) { keywords = List.of(); }

        // [기존 유지] 성별/연령대 가공 (중복 방지를 위해 totalGenderSum으로 명칭 변경)
        double femaleCur  = safeGetDone(fFemaleCur);
        double maleCur    = safeGetDone(fMaleCur);
        double totalGenderSum = femaleCur + maleCur;
        
        int gFemaleCur = totalGenderSum > 0 ? (int) Math.round(femaleCur / totalGenderSum * 100) : 50;
        int gMaleCur   = 100 - gFemaleCur;
        
        List<Integer> ageCur  = normalizeAge(fAgeCur);
        List<Integer> agePrev = normalizeAge(fAgePrev);
        String topAge         = ageCur.stream().anyMatch(v -> v > 0) ? getTopAge(ageCur) : null;
        String topGender      = totalGenderSum > 0 ? (gFemaleCur >= gMaleCur ? "여성" : "남성") : null;

        // [수정] 증감률 계산 (백분율화된 수치가 아닌 원본 지수 합계로 비교하여 정확성 유지)
        double growthPct = 0.0;
        try {
            double currentTotalRatio = fTrend.get().results().get(0).data().stream().mapToDouble(d -> d.ratio()).sum();
            double prevTotalRatio = fTrendPrev.get().results().get(0).data().stream().mapToDouble(d -> d.ratio()).sum();
            if (prevTotalRatio > 0) {
                growthPct = Math.round((currentTotalRatio - prevTotalRatio) / prevTotalRatio * 1000.0) / 10.0;
            }
        } catch (Exception e) { }

        return NaverSearchResponseDto.builder()
                .summary(NaverSearchSummaryDto.builder()
                        .totalSearchCount(summaryTotalPercent) // 100 표시
                        .searchGrowthPct(growthPct)
                        .topAgeGroup(topAge)
                        .topGender(topGender).build())
                .trends(trendList) // ✅ 수정된 백분율 리스트 반환
                .keywords(keywords)
                .ageLabels(AGE_LABELS)
                .ageCur(ageCur)
                .agePrev(agePrev)
                .genderFemaleCur(gFemaleCur)
                .genderMaleCur(gMaleCur)
                .build();
    }

    // ── 헬퍼 메서드들 (기존 유지) ────────────────────────────────────────────────
    private String getStoreName(Map<String, Object> brand) { return brand.get("brand_name").toString(); }
    private List<String> getIndustryKeywords(Map<String, Object> brand) { return List.of("강남 카페 추천", "카페 신메뉴", "디저트 맛집", "브런치 카페", "아메리카노"); }
    
    private NaverDatalabResponseDto searchWithSemaphore(NaverDatalabRequestDto request) {
        try {
            NAVER_SEMAPHORE.acquire();
            try {
                NaverDatalabResponseDto result = datalabClient.search(request);
                Thread.sleep(500);
                return result;
            } finally { NAVER_SEMAPHORE.release(); }
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private double getRatioAvg(String from, String to, String keyword, String gender, List<String> ages) {
        try {
            NAVER_SEMAPHORE.acquire();
            try {
                var resp = datalabClient.search(new NaverDatalabRequestDto(from, to, "date", List.of(new NaverDatalabRequestDto.KeywordGroup(keyword, List.of(keyword))), gender, ages));
                return resp.results().get(0).data().stream().mapToDouble(d -> d.ratio()).average().orElse(0);
            } finally { NAVER_SEMAPHORE.release(); }
        } catch (Exception e) { return 0.0; }
    }

    private double safeGetDone(CompletableFuture<Double> f) { try { return f.get(); } catch (Exception e) { return 0.0; } }

    private List<Integer> normalizeAge(List<CompletableFuture<Double>> futures) {
        List<Double> ratios = futures.stream().map(this::safeGetDone).collect(Collectors.toList());
        double total = ratios.stream().mapToDouble(Double::doubleValue).sum();
        if (total == 0) return List.of(0, 0, 0, 0, 0, 0);
        List<Integer> result = new ArrayList<>();
        int sum = 0;
        for (int i = 0; i < ratios.size() - 1; i++) {
            int pct = (int) Math.round(ratios.get(i) / total * 100);
            result.add(pct);
            sum += pct;
        }
        result.add(Math.max(0, 100 - sum));
        return result;
    }

    private String getTopAge(List<Integer> ageCur) {
        int maxIdx = 0;
        for (int i = 1; i < ageCur.size(); i++) if (ageCur.get(i) > ageCur.get(maxIdx)) maxIdx = i;
        return AGE_LABELS.get(maxIdx);
    }

    private String getPrevFrom(LocalDate from, LocalDate to) {
        long days = to.toEpochDay() - from.toEpochDay() + 1;
        return from.minusDays(days).toString();
    }

    private String formatLabel(String period, String periodType) {
        String[] parts = period.split("-");
        int mon = Integer.parseInt(parts[1]);
        int day = parts.length > 2 ? Integer.parseInt(parts[2]) : 1;
        return switch (periodType) {
            case "year"  -> mon + "월";
            case "month" -> mon + "/" + day + "주";
            default      -> mon + "/" + day;
        };
    }
}