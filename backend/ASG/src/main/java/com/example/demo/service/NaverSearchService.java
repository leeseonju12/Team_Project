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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class NaverSearchService {

    private static final Logger log = LoggerFactory.getLogger(NaverSearchService.class);

    private final NaverDatalabClient datalabClient;
    private final JdbcTemplate jdbcTemplate;

    private static final ExecutorService POOL = Executors.newFixedThreadPool(20);

    // 네이버 API 동시 호출 수 제한: Connection reset 방지를 위해 완전 순차 호출
    private static final Semaphore NAVER_SEMAPHORE = new Semaphore(1);

    private static final List<String> AGE_LABELS = List.of("10대", "20대", "30대", "40대", "50대", "60대+");

    // 나이+성별 12개 조합
    // 순서: 10대f, 10대m, 20대f, 20대m, 30대f, 30대m, 40대f, 40대m, 50대f, 50대m, 60대+f, 60대+m
    // → 짝수 인덱스=여성, 홀수=남성 / (인덱스/2) = 나이 인덱스(0~5)
    private record AgeGenderCombo(String ageCode, String ageLabel, String gender, String genderLabel) {}
    private static final List<AgeGenderCombo> AGE_GENDER_COMBOS = List.of(
        new AgeGenderCombo("1", "10대",  "f", "여성"),
        new AgeGenderCombo("1", "10대",  "m", "남성"),
        new AgeGenderCombo("2", "20대",  "f", "여성"),
        new AgeGenderCombo("2", "20대",  "m", "남성"),
        new AgeGenderCombo("3", "30대",  "f", "여성"),
        new AgeGenderCombo("3", "30대",  "m", "남성"),
        new AgeGenderCombo("4", "40대",  "f", "여성"),
        new AgeGenderCombo("4", "40대",  "m", "남성"),
        new AgeGenderCombo("5", "50대",  "f", "여성"),
        new AgeGenderCombo("5", "50대",  "m", "남성"),
        new AgeGenderCombo("6", "60대+", "f", "여성"),
        new AgeGenderCombo("6", "60대+", "m", "남성")
    );

    // 캐시 키: brandId + period만 사용
    // → from/to를 키에 넣으면 LocalDate.now()가 매번 달라져 캐시가 무의미해짐
    @Cacheable(value = "naverDashboard", key = "#brandId + '_' + #period")
    public NaverSearchResponseDto getDashboard(Long brandId, LocalDate from, LocalDate to, String period) {

        // ── 0. DB에서 브랜드 정보 조회 ───────────────────────────
        Map<String, Object> brand = jdbcTemplate.queryForMap(
                "SELECT brand_name, service_name, industry_type, location_name FROM brand WHERE brand_id = ?", brandId);
        String storeName         = getStoreName(brand);
        List<String> industryKws = getIndustryKeywords(brand);

        String timeUnit = switch (period) {
            case "week" -> "date";
            case "year" -> "month";
            default     -> "date"; // month: 일별 포인트로 촘촘하게
        };

        // week는 항상 어제 ~ 어제-6일 고정
        // → DataLab 100 기준점이 기간마다 달라지므로 고정해야 평균값이 일관성을 가짐
        LocalDate effectiveTo   = "week".equals(period) ? LocalDate.now().minusDays(1) : to;
        LocalDate effectiveFrom = "week".equals(period) ? effectiveTo.minusDays(6)    : from;

        String fromStr     = effectiveFrom.toString();
        String toStr       = effectiveTo.toString();
        String prevFromStr = getPrevFrom(effectiveFrom, effectiveTo);
        String prevToStr   = effectiveFrom.minusDays(1).toString();

        // ── 1. API 호출 목록 (총 31개) ───────────────────────────
        // [ 1]   검색 추이 현재
        // [ 2]   검색 추이 이전
        // [ 3]   업종 키워드 5개 (배치 1호출)
        // [ 4- 5] 성별 현재 (여성/남성)
        // [ 6- 7] 성별 이전 (여성/남성)
        // [ 8-19] 나이+성별 조합 현재 12개
        // [20-31] 나이+성별 조합 이전 12개
        // ※ 기존 fAgeCur/fAgePrev(12개) 제거 → 콤보에서 역산하므로 불필요

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

        // 성별 비율 차트용 (전체 성별, 나이 무관)
        var fFemaleCur  = CompletableFuture.supplyAsync(
                () -> getRatioAvg(fromStr,     toStr,     storeName, "f", null), POOL);
        var fMaleCur    = CompletableFuture.supplyAsync(
                () -> getRatioAvg(fromStr,     toStr,     storeName, "m", null), POOL);
        var fFemalePrev = CompletableFuture.supplyAsync(
                () -> getRatioAvg(prevFromStr, prevToStr, storeName, "f", null), POOL);
        var fMalePrev   = CompletableFuture.supplyAsync(
                () -> getRatioAvg(prevFromStr, prevToStr, storeName, "m", null), POOL);

        // 나이+성별 12개 조합 현재/이전
        // → 연령대 차트(ageCur/agePrev)도 이 결과에서 역산하므로 별도 fAgeCur 불필요
        List<CompletableFuture<Double>> fCombosCur = AGE_GENDER_COMBOS.stream()
                .map(c -> CompletableFuture.supplyAsync(
                        () -> getRatioAvg(fromStr, toStr, storeName, c.gender(), List.of(c.ageCode())), POOL))
                .toList();

        List<CompletableFuture<Double>> fCombosPrev = AGE_GENDER_COMBOS.stream()
                .map(c -> CompletableFuture.supplyAsync(
                        () -> getRatioAvg(prevFromStr, prevToStr, storeName, c.gender(), List.of(c.ageCode())), POOL))
                .toList();

        // 예외 발생해도 allOf가 멈추지 않도록 safe 래핑
        var fTrendSafe     = fTrend.exceptionally(e    -> { log.warn("[NaverSearch] fTrend 실패: {}",     e.getMessage()); return null; });
        var fTrendPrevSafe = fTrendPrev.exceptionally(e -> { log.warn("[NaverSearch] fTrendPrev 실패: {}", e.getMessage()); return null; });
        var fKeywordsSafe  = fKeywords.exceptionally(e  -> { log.warn("[NaverSearch] fKeywords 실패: {}",  e.getMessage()); return null; });

        List<CompletableFuture<?>> allFutures = new ArrayList<>();
        allFutures.add(fTrendSafe);
        allFutures.add(fTrendPrevSafe);
        allFutures.add(fKeywordsSafe);
        allFutures.add(fFemaleCur);
        allFutures.add(fMaleCur);
        allFutures.add(fFemalePrev);
        allFutures.add(fMalePrev);
        allFutures.addAll(fCombosCur);
        allFutures.addAll(fCombosPrev);

        // ── 2. 전체 대기 (HTTP 타임아웃 30초가 상한선) ──────────
        try {
            CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).get();
        } catch (Exception e) {
            log.error("[NaverSearch] allOf 예외: {}", e.getMessage());
        }

        // ── 3. 결과 수집 ─────────────────────────────────────────

        // 검색 추이: ratio는 이미 0~100 스케일
        List<NaverSearchTrendDto> trends;
        try {
            trends = fTrend.get().results().get(0).data().stream()
                    .map(p -> NaverSearchTrendDto.builder()
                            .label(formatLabel(p.period(), period))
                            .searchCount((int) Math.round(p.ratio()))
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

        // 성별 비율: 여성/(여성+남성)*100
        double femaleCur  = safeGetDone(fFemaleCur);
        double maleCur    = safeGetDone(fMaleCur);
        double femalePrev = safeGetDone(fFemalePrev);
        double malePrev   = safeGetDone(fMalePrev);
        double totalCur   = femaleCur  + maleCur;
        double totalPrev  = femalePrev + malePrev;
        int gFemaleCur  = totalCur  > 0 ? (int) Math.round(femaleCur  / totalCur  * 100) : 50;
        int gMaleCur    = 100 - gFemaleCur;
        int gFemalePrev = totalPrev > 0 ? (int) Math.round(femalePrev / totalPrev * 100) : 50;
        int gMalePrev   = 100 - gFemalePrev;

        // 연령대 분포: 콤보에서 나이별 여성+남성 합산 후 정규화 (fAgeCur/fAgePrev 대체)
        List<Integer> ageCur  = normalizeAgeFromCombos(fCombosCur);
        List<Integer> agePrev = normalizeAgeFromCombos(fCombosPrev);

        // 주요 사용자: 12개 조합 중 ratio 최고값 조합
        String topUser     = getTopCombo(fCombosCur);
        String prevTopUser = getTopCombo(fCombosPrev);

        // 차트용 topAge / topGender
        boolean ageHasData = ageCur.stream().anyMatch(v -> v > 0);
        String topAge      = ageHasData ? getTopAge(ageCur) : null;
        String topGender   = totalCur > 0 ? (gFemaleCur >= gMaleCur ? "여성" : "남성") : null;

        // 검색 활동량: ratio 평균 (0~100)
        int activityScore;
        try {
            activityScore = (int) Math.round(
                    fTrend.get().results().get(0).data().stream()
                            .mapToDouble(p -> p.ratio())
                            .average().orElse(0));
        } catch (Exception e) {
            log.warn("[NaverSearch] activityScore 계산 실패: {}", e.getMessage());
            activityScore = 0;
        }
        String activityStatus = getActivityStatus(activityScore);

        // 전기간 증감률
        int prevActivityScore = 0;
        try {
            prevActivityScore = (int) Math.round(
                    fTrendPrev.get().results().get(0).data().stream()
                            .mapToDouble(p -> p.ratio())
                            .average().orElse(0));
        } catch (Exception e) {
            log.warn("[NaverSearch] 이전 기간 트렌드 없음: {}", e.getMessage());
        }
        double growthPct = prevActivityScore > 0
                ? Math.round((activityScore - prevActivityScore) / (double) prevActivityScore * 1000.0) / 10.0
                : 0.0;

        log.info("[NaverSearch] 대시보드 완성 — brandId: {}, period: {}, activityScore: {}, activityStatus: {}, topUser: {}, topAge: {}, topGender: {}",
                brandId, period, activityScore, activityStatus, topUser, topAge, topGender);

        return NaverSearchResponseDto.builder()
                .summary(NaverSearchSummaryDto.builder()
                        .totalSearchCount(activityScore)
                        .searchActivityStatus(activityStatus)
                        .searchGrowthPct(growthPct)
                        .topAgeGroup(topAge)
                        .topGender(topGender)
                        .topUser(topUser)
                        .prevTopUser(prevTopUser)
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
        String industryType = (String) brand.get("industry_type");
        List<String> candidates = getCandidateKeywords(industryType); // 최대 5개 (DataLab 제한)

        // DataLab에서 오늘 기준 7일 비교 → ratio 높은 순 반환
        try {
            LocalDate to   = LocalDate.now().minusDays(1);
            LocalDate from = to.minusDays(6);

            var request = new NaverDatalabRequestDto(
                    from.toString(), to.toString(), "date",
                    candidates.stream()
                            .map(kw -> new NaverDatalabRequestDto.KeywordGroup(kw, List.of(kw)))
                            .toList());

            var resp = searchWithSemaphore(request);

            return resp.results().stream()
                    .sorted(Comparator.comparingDouble(
                            r -> -r.data().stream().mapToDouble(d -> d.ratio()).average().orElse(0)))
                    .map(r -> r.title())
                    .toList();

        } catch (Exception e) {
            log.warn("[NaverSearch] 동적 키워드 조회 실패, 기본값 사용: {}", e.getMessage());
            return getCandidateKeywords(industryType);
        }
    }

    // 업종별 후보 키워드풀 (DataLab은 한 번에 최대 5개)
    private List<String> getCandidateKeywords(String industryType) {
        return switch (industryType) {
            case "CAFE"         -> List.of("카페 추천", "카페 신메뉴", "디저트 맛집", "브런치 카페", "아메리카노");
            case "RESTAURANT"   -> List.of("맛집 추천", "점심 특선", "저녁 맛집", "혼밥", "배달 맛집");
            case "FAST_FOOD"    -> List.of("햄버거 맛집", "패스트푸드 추천", "치킨 버거", "세트메뉴", "햄버거 맛집");
            case "BAR"          -> List.of("술집 추천", "분위기 좋은 바", "이자카야 추천", "와인바", "칵테일바");
            case "BAKERY"       -> List.of("빵집 추천", "베이커리 신메뉴", "소금빵 맛집", "크루아상 맛집", "식빵 맛집");
            case "DESSERT"      -> List.of("디저트 카페", "케이크 맛집", "마카롱 추천", "아이스크림 맛집", "빙수 맛집");
            case "HAIR_SALON"   -> List.of("미용실 추천", "헤어샵 예약", "염색 잘하는 곳", "커트 잘하는 미용실", "펌 추천");
            case "NAIL"         -> List.of("네일샵 추천", "젤네일 디자인", "네일아트 예약", "발네일 추천", "네일 가격");
            case "SPA_MASSAGE"  -> List.of("마사지샵 추천", "스파 예약", "피부관리 잘하는 곳", "아로마 마사지", "힐링 스파");
            case "FITNESS"      -> List.of("헬스장 추천", "PT 가격", "크로스핏 추천", "헬스 등록", "퍼스널트레이닝");
            case "PILATES_YOGA" -> List.of("필라테스 추천", "요가 학원", "필라테스 가격", "요가 초보", "몸매관리 운동");
            case "RETAIL"       -> List.of("편집샵 추천", "신상 입고", "한정판 구매", "편집샵 세일", "브랜드 추천");
            case "CLOTHING"     -> List.of("옷 쇼핑", "빈티지샵 추천", "코디 추천", "의류 세일", "가을 신상");
            case "LAUNDRY"      -> List.of("세탁소 추천", "드라이클리닝 가격", "이불 세탁", "명품 세탁", "운동화 세탁");
            case "PET"          -> List.of("펫샵 추천", "강아지 용품", "고양이 간식 추천", "동물병원 추천", "반려동물 미용");
            case "EDUCATION"    -> List.of("학원 추천", "과외 추천", "수능 학원", "성인 영어학원", "자격증 학원");
            default             -> List.of("매장 추천", "신메뉴", "할인 이벤트", "맛집", "베스트메뉴");
        };
    }
    // ── 최대 3회 재시도 + Semaphore 순차 호출 ────────────────────
    private static final int  MAX_RETRY      = 3;
    private static final long RETRY_DELAY_MS = 5000; // 재시도 전 5초 대기

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
                log.warn("[NaverSearch] 호출 실패 ({}회/{}회): {}", attempt, MAX_RETRY, e.getMessage());
                if (attempt < MAX_RETRY) {
                    try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }
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
                    log.info("[NaverSearch] 호출 성공 — keyword: {}, gender: {}, ages: {}, ratio: {}",
                            keyword, gender, ages, result);
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
                log.warn("[NaverSearch] getRatioAvg 실패 ({}회/{}회, gender={}, ages={}): {}",
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
        try { return f.get(); } catch (Exception e) { return 0.0; }
    }

    // ── 콤보 결과에서 연령대 분포 역산 ────────────────────────────
    // 콤보 순서: [0]=10대f, [1]=10대m, [2]=20대f ... (짝=f, 홀=m)
    // 나이 인덱스(0~5) = 콤보 인덱스 / 2  →  여성+남성 합산 = 해당 나이대 활동량
    private List<Integer> normalizeAgeFromCombos(List<CompletableFuture<Double>> combos) {
        List<Double> ageRatios = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            double female = safeGetDone(combos.get(i * 2));
            double male   = safeGetDone(combos.get(i * 2 + 1));
            ageRatios.add(female + male);
        }
        double total = ageRatios.stream().mapToDouble(Double::doubleValue).sum();
        if (total == 0) return List.of(0, 0, 0, 0, 0, 0);

        List<Integer> result = new ArrayList<>();
        int sum = 0;
        for (int i = 0; i < ageRatios.size() - 1; i++) {
            int pct = (int) Math.round(ageRatios.get(i) / total * 100);
            result.add(pct);
            sum += pct;
        }
        result.add(Math.max(0, 100 - sum)); // 반올림 오차 보정
        return result;
    }

    // ── 나이+성별 조합 중 ratio 최고값 그룹 반환 ─────────────────
    private String getTopCombo(List<CompletableFuture<Double>> futures) {
        double maxRatio = -1;
        int maxIdx = -1;
        for (int i = 0; i < futures.size(); i++) {
            double v = safeGetDone(futures.get(i));
            if (v > maxRatio) { maxRatio = v; maxIdx = i; }
        }
        if (maxIdx < 0 || maxRatio == 0) return null;
        AgeGenderCombo c = AGE_GENDER_COMBOS.get(maxIdx);
        return c.ageLabel() + " " + c.genderLabel(); // 예: "30대 여성"
    }

    // ── 검색 활동 상태 분류 ────────────────────────────────────────
    // 임계값은 실제 데이터 누적 후 튜닝 권장
    private String getActivityStatus(int avgScore) {
        if (avgScore >= 70) return "폭발적";
        if (avgScore >= 40) return "안정적";
        return "침체기";
    }

    // ── 매장명 ───────────────────────────────────────────────────
    private String getStoreName(Map<String, Object> brand) {
        return brand.get("brand_name").toString();
    }

    // ── 최다 연령대 추출 (차트용) ─────────────────────────────────
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
            case "month" -> mon + "/" + day;
            default      -> mon + "/" + day;
        };
    }
}