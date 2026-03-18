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

    // 네이버 API 동시 호출 수 제한: 5개 초과 시 Connection reset 발생
    private static final Semaphore NAVER_SEMAPHORE = new Semaphore(1); // 완전 순차 호출 - 동시 호출 시 Connection reset 방지

    private static final List<String> AGE_CODES  = List.of("1", "2", "3", "4", "5", "6");
    private static final List<String> AGE_LABELS = List.of("10대", "20대", "30대", "40대", "50대", "60대+");

    // 캐시 키: brandId + period만 사용
    // → 같은 브랜드·기간 단위는 1시간 내 재호출 시 캐시에서 반환 (API 미호출)
    // → from/to를 키에 넣으면 LocalDate.now()가 매번 달라져 캐시가 무의미해짐
    @Cacheable(value = "naverDashboard", key = "#brandId + '_' + #period")
    public NaverSearchResponseDto getDashboard(Long brandId, LocalDate from, LocalDate to, String period) {

        // ── 0. DB에서 브랜드 정보 조회 ───────────────────────────
        Map<String, Object> brand = jdbcTemplate.queryForMap(
                "SELECT brand_name, service_name, industry_type, location_name FROM brand WHERE brand_id = ?", brandId);
        String storeName   = getStoreName(brand);
        List<String> industryKws = getIndustryKeywords(brand);

        String timeUnit    = switch (period) {
            case "week"  -> "date";
            case "year"  -> "month";
            default      -> "week";
        };
        String fromStr     = from.toString();
        String toStr       = to.toString();
        String prevFromStr = getPrevFrom(from, to);
        String prevToStr   = from.minusDays(1).toString();

        // ── 1. 병렬 API 호출 (19개 → 17개) ─────────────────────

        // Semaphore로 감싼 search 호출: 동시 5개 초과 시 대기
        var fTrend = CompletableFuture.supplyAsync(() -> searchWithSemaphore(
                new NaverDatalabRequestDto(fromStr, toStr, timeUnit,
                        List.of(new NaverDatalabRequestDto.KeywordGroup(storeName, List.of(storeName))))), POOL);

        var fTrendPrev = CompletableFuture.supplyAsync(() -> searchWithSemaphore(
                new NaverDatalabRequestDto(prevFromStr, prevToStr, timeUnit,
                        List.of(new NaverDatalabRequestDto.KeywordGroup(storeName, List.of(storeName))))), POOL);

        var fKeywords = CompletableFuture.supplyAsync(() -> searchWithSemaphore(
                new NaverDatalabRequestDto(fromStr, toStr, timeUnit,
                        industryKws.stream()
                                .map(kw -> new NaverDatalabRequestDto.KeywordGroup(kw, List.of(kw)))
                                .toList())), POOL);

        // 성별은 여성+남성 지수 둘 다 받아야 비율 계산 가능
        // 여성/(여성+남성)*100 → 여성% , 남성% = 100 - 여성%
        var fFemaleCur  = CompletableFuture.supplyAsync(
                () -> getRatioAvg(fromStr,     toStr,     storeName, "f", null), POOL);
        var fMaleCur    = CompletableFuture.supplyAsync(
                () -> getRatioAvg(fromStr,     toStr,     storeName, "m", null), POOL);
        var fFemalePrev = CompletableFuture.supplyAsync(
                () -> getRatioAvg(prevFromStr, prevToStr, storeName, "f", null), POOL);
        var fMalePrev   = CompletableFuture.supplyAsync(
                () -> getRatioAvg(prevFromStr, prevToStr, storeName, "m", null), POOL);

        List<CompletableFuture<Double>> fAgeCur = AGE_CODES.stream()
                .map(age -> CompletableFuture.supplyAsync(
                        () -> getRatioAvg(fromStr, toStr, storeName, null, List.of(age)), POOL))
                .toList();

        List<CompletableFuture<Double>> fAgePrev = AGE_CODES.stream()
                .map(age -> CompletableFuture.supplyAsync(
                        () -> getRatioAvg(prevFromStr, prevToStr, storeName, null, List.of(age)), POOL))
                .toList();

        // 각 Future가 예외를 던져도 allOf가 중단되지 않도록 exceptionally로 감쌈
        // → 하나가 Connection reset 나도 나머지 16개는 계속 진행
        var fTrendSafe    = fTrend.exceptionally(e -> { log.warn("[NaverSearch] fTrend 실패: {}", e.getMessage()); return null; });
        var fTrendPrevSafe = fTrendPrev.exceptionally(e -> { log.warn("[NaverSearch] fTrendPrev 실패: {}", e.getMessage()); return null; });
        var fKeywordsSafe  = fKeywords.exceptionally(e -> { log.warn("[NaverSearch] fKeywords 실패: {}", e.getMessage()); return null; });

        List<CompletableFuture<?>> allFutures = new ArrayList<>();
        allFutures.add(fTrendSafe);
        allFutures.add(fTrendPrevSafe);
        allFutures.add(fKeywordsSafe);
        allFutures.add(fFemaleCur);
        allFutures.add(fMaleCur);
        allFutures.add(fFemalePrev);
        allFutures.add(fMalePrev);
        allFutures.addAll(fAgeCur);
        allFutures.addAll(fAgePrev);

        // ── 2. 전체 대기 (타임아웃 없음 - 모든 데이터를 반드시 수신)
        // 네이버 서버 무응답 시에는 HTTP 타임아웃(30초)이 상한선
        try {
            CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).get();
        } catch (Exception e) {
            log.error("[NaverSearch] allOf 예외: {}", e.getMessage());
        }

        // ── 3. 결과 수집 ─────────────────────────────────────────

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
            log.warn("[NaverSearch] 트렌드 데이터 없음: {}", e.getMessage());
            trends = List.of();
        }

        // 동종업계 키워드 순위
        List<NaverKeywordDto> keywords = new ArrayList<>();
        try {
            var kwResults = fKeywords.get().results();
            for (int i = 0; i < kwResults.size(); i++) {
                var r   = kwResults.get(i);
                int avg = (int) Math.round(
                        r.data().stream().mapToDouble(d -> d.ratio()).average().orElse(0) * 100);
                keywords.add(NaverKeywordDto.builder()
                        .keywordText(r.title()).searchCount(avg).rankNo(i + 1).build());
            }
            keywords.sort((a, b) -> b.searchCount() - a.searchCount());
            for (int i = 0; i < keywords.size(); i++) {
                var k = keywords.get(i);
                keywords.set(i, NaverKeywordDto.builder()
                        .keywordText(k.keywordText()).searchCount(k.searchCount()).rankNo(i + 1).build());
            }
        } catch (Exception e) {
            log.warn("[NaverSearch] 키워드 데이터 없음: {}", e.getMessage());
            keywords = List.of();
        }

        // 성별 비율: 여성지수 / (여성지수 + 남성지수) * 100
        double femaleCur  = safeGetDone(fFemaleCur);
        double maleCur    = safeGetDone(fMaleCur);
        double femalePrev = safeGetDone(fFemalePrev);
        double malePrev   = safeGetDone(fMalePrev);
        double totalCur   = femaleCur  + maleCur;
        double totalPrev  = femalePrev + malePrev;
        // 둘 다 0이면 50:50 폴백
        int gFemaleCur  = totalCur  > 0 ? (int) Math.round(femaleCur  / totalCur  * 100) : 50;
        int gMaleCur    = 100 - gFemaleCur;
        int gFemalePrev = totalPrev > 0 ? (int) Math.round(femalePrev / totalPrev * 100) : 50;
        int gMalePrev   = 100 - gFemalePrev;

        // 연령대 분포
        List<Integer> ageCur  = normalizeAge(fAgeCur);
        List<Integer> agePrev = normalizeAge(fAgePrev);

        // 주요 사용자 요약
        boolean ageHasData    = ageCur.stream().anyMatch(v -> v > 0);
        String topAge         = ageHasData ? getTopAge(ageCur) : null;
        String topGender      = totalCur > 0 ? (gFemaleCur >= gMaleCur ? "여성" : "남성") : null;
        int totalSearch       = trends.stream().mapToInt(NaverSearchTrendDto::searchCount).sum();

        // 전기간 증감률
        int prevTotalSearch = 0;
        try {
            prevTotalSearch = fTrendPrev.get().results().get(0).data().stream()
                    .mapToInt(p -> (int) Math.round(p.ratio() * 100)).sum();
        } catch (Exception e) {
            log.warn("[NaverSearch] 이전 기간 트렌드 없음: {}", e.getMessage());
        }
        double growthPct = prevTotalSearch > 0
                ? Math.round((totalSearch - prevTotalSearch) / (double) prevTotalSearch * 1000.0) / 10.0
                : 0.0;

        log.info("[NaverSearch] 대시보드 완성 — brandId: {}, period: {}, totalSearch: {}, topAge: {}, topGender: {}", brandId, period, totalSearch, topAge, topGender);
        return NaverSearchResponseDto.builder()
                .summary(NaverSearchSummaryDto.builder()
                        .totalSearchCount(totalSearch)
                        .searchGrowthPct(growthPct)
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

    // ── 업종별 동종업계 키워드 ────────────────────────────────────
    // TODO: 업종/브랜드별 동적 키워드 로직으로 교체 예정
    private List<String> getIndustryKeywords(Map<String, Object> brand) {
        return List.of("강남 카페 추천", "카페 신메뉴", "디저트 맛집", "브런치 카페", "아메리카노");
    }

    // ── 최대 3회 재시도 + Semaphore 동시 호출 제한 ──────────────
    // Connection reset / timeout 은 일시적 차단이므로 2초 대기 후 재시도하면 대부분 성공
    private static final int    MAX_RETRY       = 3;
    private static final long   RETRY_DELAY_MS  = 5000; // 재시도 전 대기 5초 (네이버 차단 해제 대기)

    private NaverDatalabResponseDto searchWithSemaphore(NaverDatalabRequestDto request) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                NAVER_SEMAPHORE.acquire();
                try {
                    NaverDatalabResponseDto result = datalabClient.search(request);
                    log.info("[NaverSearch] 호출 성공 — keyword: {}", request.keywordGroups().get(0).groupName());
                    Thread.sleep(500);
                    return result;
                } finally {
                    NAVER_SEMAPHORE.release();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Semaphore 대기 중 인터럽트", e);
            } catch (Exception e) {
                lastException = e;
                log.warn("[NaverSearch] 호출 실패 ({}회/{} 회): {}", attempt, MAX_RETRY, e.getMessage());
                if (attempt < MAX_RETRY) {
                    try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }
        // 3회 모두 실패 시 예외 → exceptionally에서 잡아서 로그만 남기고 null 반환
        throw new RuntimeException("네이버 API " + MAX_RETRY + "회 재시도 모두 실패", lastException);
    }

    // ── 단일 API 호출 평균 ratio (재시도 포함) ────────────────────
    private double getRatioAvg(String from, String to, String keyword, String gender, List<String> ages) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                NAVER_SEMAPHORE.acquire();
                try {
                    var resp = datalabClient.search(new NaverDatalabRequestDto(from, to, "date",
                            List.of(new NaverDatalabRequestDto.KeywordGroup(keyword, List.of(keyword))),
                            gender, ages));
                    double result = resp.results().get(0).data().stream()
                            .mapToDouble(d -> d.ratio()).average().orElse(0);
                    log.info("[NaverSearch] 호출 성공 — keyword: {}, gender: {}, ages: {}, ratio: {}", keyword, gender, ages, result);
                    Thread.sleep(500);
                    return result;
                } finally {
                    NAVER_SEMAPHORE.release();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return 0.0;
            } catch (Exception e) {
                lastException = e;
                log.warn("[NaverSearch] getRatioAvg 실패 ({}회/{} 회, gender={}, ages={}): {}",
                        attempt, MAX_RETRY, gender, ages, e.getMessage());
                if (attempt < MAX_RETRY) {
                    try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }
        log.error("[NaverSearch] getRatioAvg 최종 실패 (gender={}, ages={}): {}", gender, ages,
                lastException != null ? lastException.getMessage() : "unknown");
        return 0.0;
    }

    // ── allOf 이후 완료된 Future 값 꺼내기 ───────────────────────
    private double safeGetDone(CompletableFuture<Double> f) {
        try {
            return f.get();  // allOf 이후이므로 항상 완료 상태
        } catch (Exception e) {
            return 0.0;
        }
    }

    // ── 연령대 비율 정규화 → 합계 100% ───────────────────────────
    private List<Integer> normalizeAge(List<CompletableFuture<Double>> futures) {
        List<Double> ratios = futures.stream().map(f -> {
            try { return f.get(); }  // allOf 이후이므로 항상 완료 상태
            catch (Exception e) { return 0.0; }
        }).collect(Collectors.toList());

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

    // ── 매장명 ───────────────────────────────────────────────────
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