package com.example.demo.service;

import com.example.demo.dto.channel.*;
import lombok.RequiredArgsConstructor;
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

    private final NaverDatalabClient datalabClient;
    private final JdbcTemplate jdbcTemplate;

    // 14번 동시 호출 → 스레드 16개짜리 전용 풀
    private static final ExecutorService POOL =
        Executors.newFixedThreadPool(16);

    // API 1개당 최대 대기 시간 (초)
    private static final int TIMEOUT_SEC = 5;

    private static final List<String> AGE_CODES  = List.of("1","2","3","4","5","6");
    private static final List<String> AGE_LABELS = List.of("10대","20대","30대","40대","50대","60대+");

    public NaverSearchResponseDto getDashboard(Long brandId, LocalDate from, LocalDate to, String period) {

        // ── 0. DB에서 브랜드 정보 조회 ───────────────────────────
        // TODO: 로그인 기능 구현 시 세션/토큰에서 brandId 추출로 교체
        Map<String, Object> brand = jdbcTemplate.queryForMap(
            "SELECT brand_name, service_name, industry_type, location_name FROM brand WHERE brand_id = ?",
            brandId
        );
        String storeName         = getStoreName(brand);
        List<String> industryKws = getIndustryKeywords(brand);

        String timeUnit = switch (period) {
            case "week"  -> "date";
            case "year"  -> "month";
            default      -> "week";
        };
        String fromStr     = from.toString();
        String toStr       = to.toString();
        String prevFromStr = getPrevFrom(from, to);
        String prevToStr   = from.minusDays(1).toString();

        // ── 1. 모든 API 병렬 호출 ────────────────────────────────

        // 내 매장명 검색수 추이
        final String storeNameFinal = storeName;
        var fTrend = CompletableFuture.supplyAsync(() ->
            datalabClient.search(new NaverDatalabRequestDto(
                fromStr, toStr, timeUnit,
                List.of(new NaverDatalabRequestDto.KeywordGroup(storeNameFinal, List.of(storeNameFinal)))
            )), POOL
        );

        // 동종업계 인기 키워드
        final List<String> kwsFinal = industryKws;
        var fKeywords = CompletableFuture.supplyAsync(() ->
            datalabClient.search(new NaverDatalabRequestDto(
                fromStr, toStr, timeUnit,
                kwsFinal.stream()
                    .map(kw -> new NaverDatalabRequestDto.KeywordGroup(kw, List.of(kw)))
                    .toList()
            )), POOL
        );

        // 성별 (현재/이전 기간)
        var fFemaleCur  = CompletableFuture.supplyAsync(() -> getRatioAvg(fromStr,     toStr,     storeNameFinal, "f", null), POOL);
        var fMaleCur    = CompletableFuture.supplyAsync(() -> getRatioAvg(fromStr,     toStr,     storeNameFinal, "m", null), POOL);
        var fFemalePrev = CompletableFuture.supplyAsync(() -> getRatioAvg(prevFromStr, prevToStr, storeNameFinal, "f", null), POOL);
        var fMalePrev   = CompletableFuture.supplyAsync(() -> getRatioAvg(prevFromStr, prevToStr, storeNameFinal, "m", null), POOL);

        // 연령대 (현재/이전 기간) — 각 6개 동시 호출
        List<CompletableFuture<Double>> fAgeCur = AGE_CODES.stream()
            .map(age -> CompletableFuture.supplyAsync(() ->
                getRatioAvg(fromStr, toStr, storeNameFinal, null, List.of(age)), POOL))
            .toList();

        List<CompletableFuture<Double>> fAgePrev = AGE_CODES.stream()
            .map(age -> CompletableFuture.supplyAsync(() ->
                getRatioAvg(prevFromStr, prevToStr, storeNameFinal, null, List.of(age)), POOL))
            .toList();

        // ── 2. 결과 수집 ─────────────────────────────────────────

        // 검색수 추이
        List<NaverSearchTrendDto> trends;
        try {
            trends = fTrend.get().results().get(0).data().stream()
                .map(p -> NaverSearchTrendDto.builder()
                    .label(formatLabel(p.period(), period))
                    .searchCount((int) Math.round(p.ratio() * 100))
                    .build())
                .toList();
        } catch (Exception e) {
            trends = List.of();
        }

        // 동종업계 키워드 순위
        List<NaverKeywordDto> keywords = new ArrayList<>();
        try {
            var kwResults = fKeywords.get().results();
            for (int i = 0; i < kwResults.size(); i++) {
                var r = kwResults.get(i);
                int avg = (int) Math.round(
                    r.data().stream().mapToDouble(d -> d.ratio()).average().orElse(0) * 100
                );
                keywords.add(NaverKeywordDto.builder()
                    .keywordText(r.title())
                    .searchCount(avg)
                    .rankNo(i + 1)
                    .build());
            }
            keywords.sort((a, b) -> b.searchCount() - a.searchCount());
            for (int i = 0; i < keywords.size(); i++) {
                var k = keywords.get(i);
                keywords.set(i, NaverKeywordDto.builder()
                    .keywordText(k.keywordText())
                    .searchCount(k.searchCount())
                    .rankNo(i + 1).build());
            }
        } catch (Exception e) {
            keywords = List.of();
        }

        // 성별 비율
        double femaleCur  = safeGet(fFemaleCur,  50.0);
        double maleCur    = safeGet(fMaleCur,    50.0);
        double femalePrev = safeGet(fFemalePrev, 50.0);
        double malePrev   = safeGet(fMalePrev,   50.0);
        double total      = femaleCur + maleCur;
        double totalPrev  = femalePrev + malePrev;
        int gFemaleCur  = total     > 0 ? (int) Math.round(femaleCur  / total     * 100) : 50;
        int gMaleCur    = 100 - gFemaleCur;
        int gFemalePrev = totalPrev > 0 ? (int) Math.round(femalePrev / totalPrev * 100) : 50;
        int gMalePrev   = 100 - gFemalePrev;

        // 연령대 분포
        List<Integer> ageCur  = normalizeAge(fAgeCur);
        List<Integer> agePrev = normalizeAge(fAgePrev);

        // 주요 사용자 요약
        boolean ageHasData    = ageCur.stream().anyMatch(v -> v > 0);
        boolean genderHasData = total > 0;
        String topAge    = ageHasData    ? getTopAge(ageCur)                          : null;
        String topGender = genderHasData ? (gFemaleCur >= gMaleCur ? "여성" : "남성") : null;
        int totalSearch  = trends.stream().mapToInt(NaverSearchTrendDto::searchCount).sum();

        return NaverSearchResponseDto.builder()
            .summary(NaverSearchSummaryDto.builder()
                .totalSearchCount(totalSearch)
                .searchGrowthPct(0.0)
                .topAgeGroup(topAge)
                .topGender(topGender)
                .build())
            .trends(trends)
            .keywords(keywords)
            .ageLabels(AGE_LABELS)
            .ageCur(ageCur)
            .agePrev(agePrev)
            .genderFemaleCur(gFemaleCur)
            .genderMaleCur(gMaleCur)
            .genderFemalePrev(gFemalePrev)
            .genderMalePrev(gMalePrev)
            .build();
    }

    // ── 업종별 동종업계 키워드 (최대 5개) ─────────────────────────
    // TODO: 업종/브랜드별 동적 키워드 로직으로 교체 예정
    // 데이터랩 - 검색어 트렌드에서 결과 받아옴
    private List<String> getIndustryKeywords(Map<String, Object> brand) {
        return List.of(
            "강남 카페 추천",
            "카페 신메뉴",
            "디저트 맛집",
            "브런치 카페",
            "아메리카노"
        );
    }

    // ── 헬퍼: 단일 API 호출 평균 ratio ───────────────────────────
    private double getRatioAvg(String from, String to, String keyword,
                               String gender, List<String> ages) {
        try {
            var resp = datalabClient.search(new NaverDatalabRequestDto(
                from, to, "date",
                List.of(new NaverDatalabRequestDto.KeywordGroup(keyword, List.of(keyword))),
                gender, ages
            ));
            return resp.results().get(0).data().stream()
                .mapToDouble(d -> d.ratio()).average().orElse(0);
        } catch (Exception e) {
            return 0.0;
        }
    }

    // ── 헬퍼: Future 안전 get (타임아웃 적용) ────────────────────
    private double safeGet(CompletableFuture<Double> f, double defaultVal) {
        try { return f.get(TIMEOUT_SEC, TimeUnit.SECONDS); }
        catch (Exception e) { return defaultVal; }
    }

    // ── 연령대 비율 정규화 ───────────────────────────
    private List<Integer> normalizeAge(List<CompletableFuture<Double>> futures) {
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(TIMEOUT_SEC, TimeUnit.SECONDS);
        } catch (Exception ignored) {}

        List<Double> ratios = futures.stream()
            .map(f -> {
                try { return f.isDone() ? f.get() : 0.0; }
                catch (Exception e) { return 0.0; }
            })
            .collect(Collectors.toList());

        double total = ratios.stream().mapToDouble(Double::doubleValue).sum();
        List<Integer> result = new ArrayList<>();
        int sum = 0;
        for (int i = 0; i < ratios.size() - 1; i++) {
            int pct = total > 0 ? (int) Math.round(ratios.get(i) / total * 100) : 0;
            result.add(pct);
            sum += pct;
        }
        result.add(Math.max(0, 100 - sum));
        return result;
    }

    // ── 매장명: brand_name 기준 ───────────────────────────────────
    private String getStoreName(Map<String, Object> brand) {
        return brand.get("brand_name").toString();
    }

    // ── 최다 연령대 추출 ──────────────────────────────────────────
    private String getTopAge(List<Integer> ageCur) {
        int maxIdx = 0;
        for (int i = 1; i < ageCur.size(); i++) {
            if (ageCur.get(i) > ageCur.get(maxIdx)) maxIdx = i;
        }
        return AGE_LABELS.get(maxIdx);
    }

    // ── 이전 기간 시작일 ──────────────────────────────────────────
    private String getPrevFrom(LocalDate from, LocalDate to) {
        long days = to.toEpochDay() - from.toEpochDay() + 1;
        return from.minusDays(days).toString();
    }

    // ── 레이블 포맷 ───────────────────────────────────────────────
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