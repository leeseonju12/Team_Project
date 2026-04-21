package com.example.demo.service;

import com.example.demo.dto.channel.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChannelPerformanceService {

    private final JdbcTemplate jdbcTemplate;
    private static final DateTimeFormatter DATE_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public ChannelPerformanceResponseDto getDashboard(Long brandId, LocalDate fromDate, LocalDate toDate) {
        if (fromDate.isAfter(toDate)) {
            LocalDate temp = fromDate;
            fromDate = toDate;
            toDate = temp;
        }

        int fromDateKey = Integer.parseInt(fromDate.format(DATE_KEY_FORMATTER));
        int toDateKey   = Integer.parseInt(toDate.format(DATE_KEY_FORMATTER));

        return ChannelPerformanceResponseDto.builder()
                .brandId(brandId)
                .from(fromDate.toString())
                .to(toDate.toString())
                .summaries(findChannelSummaries(brandId, fromDateKey, toDateKey))
                .trends(findDailyTrends(brandId, fromDateKey, toDateKey))
                .topPosts(findTopPosts(brandId, fromDateKey, toDateKey))
                .insight(findLatestInsight(brandId))
                .reviewTrend(findReviewTrend(brandId, fromDateKey, toDateKey, fromDate, toDate))
                .build();
    }

    // ── 1. 채널별 집계 요약 ──────────────────────────────────────────
    private List<ChannelPerformanceSummaryDto> findChannelSummaries(Long brandId, int fromDateKey, int toDateKey) {
        String sql = """
                SELECT bp.brand_platform_id,
                       p.platform_code,
                       p.platform_name,
                       COALESCE(SUM(pmd.total_likes),    0) AS total_likes,
                       COALESCE(SUM(pmd.total_comments), 0) AS total_comments,
                       COALESCE(SUM(pmd.total_shares),   0) AS total_shares,
                       COALESCE(SUM(pmd.total_reviews),  0) AS total_reviews,
                       COALESCE(SUM(pmd.follower_growth),0) AS follower_growth,
                       COALESCE(AVG(pmd.engagement_score), 0) AS engagement_rate
                FROM brand_platform bp
                JOIN platform p ON p.platform_id = bp.platform_id
                LEFT JOIN platform_metric_daily pmd
                       ON pmd.brand_platform_id = bp.brand_platform_id
                      AND pmd.date_key BETWEEN ? AND ?
                WHERE bp.brand_id = ?
                GROUP BY bp.brand_platform_id, p.platform_code, p.platform_name
                ORDER BY total_likes DESC
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> ChannelPerformanceSummaryDto.builder()
                        .brandPlatformId(rs.getLong("brand_platform_id"))
                        .platformCode(rs.getString("platform_code"))
                        .platformName(rs.getString("platform_name"))
                        .totalLikes(rs.getInt("total_likes"))
                        .totalComments(rs.getInt("total_comments"))
                        .totalShares(rs.getInt("total_shares"))
                        .totalReviews(rs.getInt("total_reviews"))
                        .followerGrowth(rs.getInt("follower_growth"))
                        .engagementRate(rs.getDouble("engagement_rate"))
                        .build(),
                fromDateKey, toDateKey, brandId);
    }

    // ── 2. 일별 트렌드 ───────────────────────────────────────────────
    private List<ChannelPerformanceTrendPointDto> findDailyTrends(Long brandId, int fromDateKey, int toDateKey) {
        String sql = """
                SELECT pmd.date_key,
                       COALESCE(SUM(pmd.total_likes),    0) AS likes,
                       COALESCE(SUM(pmd.total_comments), 0) AS comments,
                       COALESCE(SUM(pmd.total_shares),   0) AS shares,
                       COALESCE(SUM(pmd.total_reviews),  0) AS reviews
                FROM platform_metric_daily pmd
                JOIN brand_platform bp ON bp.brand_platform_id = pmd.brand_platform_id
                WHERE bp.brand_id = ?
                  AND pmd.date_key BETWEEN ? AND ?
                GROUP BY pmd.date_key
                ORDER BY pmd.date_key
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> ChannelPerformanceTrendPointDto.builder()
                        .dateKey(rs.getInt("date_key"))
                        .likes(rs.getInt("likes"))
                        .comments(rs.getInt("comments"))
                        .shares(rs.getInt("shares"))
                        .reviews(rs.getInt("reviews"))
                        .build(),
                brandId, fromDateKey, toDateKey);
    }

    // ── 3. 상위 게시물 ───────────────────────────────────────────────
    private List<ChannelTopPostDto> findTopPosts(Long brandId, int fromDateKey, int toDateKey) {
        String sql = """
                SELECT cp.post_id,
                       COALESCE(cp.post_title, CONCAT('Post #', cp.post_id)) AS post_title,
                       p.platform_name,
                       COALESCE(SUM(pmd.like_count),    0) AS likes,
                       COALESCE(SUM(pmd.comment_count), 0) AS comments,
                       COALESCE(SUM(pmd.share_count),   0) AS shares,
                       COALESCE(SUM(pmd.review_count),  0) AS review_count,
                       COALESCE(SUM(pmd.like_count + pmd.comment_count + pmd.share_count), 0) AS engagement_rate
                FROM content_post cp
                JOIN brand_platform bp ON bp.brand_platform_id = cp.brand_platform_id
                JOIN platform p ON p.platform_id = bp.platform_id
                LEFT JOIN post_metric_daily pmd
                       ON pmd.post_id = cp.post_id
                      AND pmd.date_key BETWEEN ? AND ?
                WHERE bp.brand_id = ?
                GROUP BY cp.post_id, cp.post_title, p.platform_name
                ORDER BY engagement_rate DESC
                LIMIT 5
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> ChannelTopPostDto.builder()
                        .postId(rs.getLong("post_id"))
                        .postTitle(rs.getString("post_title"))
                        .platformName(rs.getString("platform_name"))
                        .likes(rs.getInt("likes"))
                        .comments(rs.getInt("comments"))
                        .shares(rs.getInt("shares"))
                        .reviewCount(rs.getInt("review_count"))
                        .engagementRate(rs.getDouble("engagement_rate"))
                        .build(),
                fromDateKey, toDateKey, brandId);
    }

    // ── 4. 리뷰 트렌드 (플랫폼별 일간 → 집계) ───────────────────────
    private ReviewTrendDto findReviewTrend(Long brandId, int fromDateKey, int toDateKey,
                                           LocalDate fromDate, LocalDate toDate) {
        String sql = """
                SELECT p.platform_code,
                       pmd.date_key,
                       COALESCE(SUM(pmd.total_reviews), 0) AS reviews
                FROM platform_metric_daily pmd
                JOIN brand_platform bp ON bp.brand_platform_id = pmd.brand_platform_id
                JOIN platform p ON p.platform_id = bp.platform_id
                WHERE bp.brand_id = ?
                  AND pmd.date_key BETWEEN ? AND ?
                GROUP BY p.platform_code, pmd.date_key
                ORDER BY pmd.date_key
                """;

        // date_key → platformCode → reviews 맵
        Map<Integer, Map<String, Integer>> dataMap = new LinkedHashMap<>();
        Set<String> platforms = new LinkedHashSet<>();

        jdbcTemplate.query(sql, rs -> {
            String code   = rs.getString("platform_code");
            int dateKey   = rs.getInt("date_key");
            int reviews   = rs.getInt("reviews");
            platforms.add(code);
            dataMap.computeIfAbsent(dateKey, k -> new HashMap<>()).put(code, reviews);
        }, brandId, fromDateKey, toDateKey);

        List<String> labels  = new ArrayList<>();
        List<Integer> google = new ArrayList<>();
        List<Integer> naver  = new ArrayList<>();
        List<Integer> kakao  = new ArrayList<>();
        List<Integer> total  = new ArrayList<>();

        for (Map.Entry<Integer, Map<String, Integer>> entry : dataMap.entrySet()) {
            String dk = String.valueOf(entry.getKey());
            // date_key(20260301) → "03/01" 형식
            labels.add(dk.substring(4, 6) + "/" + dk.substring(6, 8));

            Map<String, Integer> row = entry.getValue();
            int g = row.getOrDefault("google", 0);
            int n = row.getOrDefault("naver",  0);
            int k = row.getOrDefault("kakao",  0);
            google.add(g);
            naver.add(n);
            kakao.add(k);
            total.add(g + n + k);
        }

        long days = fromDate.until(toDate).getDays() + 1;
        String period = days <= 7 ? "week" : days <= 31 ? "month" : "year";

        return ReviewTrendDto.builder()
                .period(period)
                .labels(labels)
                .google(google)
                .naver(naver)
                .kakao(kakao)
                .total(total)
                .build();
    }

    // ── 5. 인사이트 ─────────────────────────────────────────────────
    private ChannelPerformanceInsightDto findLatestInsight(Long brandId) {
        String sql = """
                SELECT pia.analysis_period_type,
                       pia.base_year,
                       pia.base_month,
                       pia.weekend_effect_score,
                       pia.holiday_effect_score,
                       pia.best_day_of_week,
                       pia.best_hour_range
                FROM performance_impact_analysis pia
                JOIN brand_platform bp ON bp.brand_platform_id = pia.brand_platform_id
                WHERE bp.brand_id = ?
                ORDER BY pia.base_year DESC, pia.base_month DESC
                LIMIT 1
                """;

        List<ChannelPerformanceInsightDto> results = jdbcTemplate.query(sql,
                (rs, rowNum) -> ChannelPerformanceInsightDto.builder()
                        .periodType(rs.getString("analysis_period_type"))
                        .baseYear((Integer) rs.getObject("base_year"))
                        .baseMonth((Integer) rs.getObject("base_month"))
                        .weekendEffectScore(rs.getObject("weekend_effect_score") != null
                                ? rs.getDouble("weekend_effect_score") : null)
                        .holidayEffectScore(rs.getObject("holiday_effect_score") != null
                                ? rs.getDouble("holiday_effect_score") : null)
                        .bestDayOfWeek((Integer) rs.getObject("best_day_of_week"))
                        .bestHourRange(rs.getString("best_hour_range"))
                        .build(),
                brandId);

        if (results.isEmpty()) {
            return ChannelPerformanceInsightDto.builder()
                    .periodType("month")
                    .baseYear(null)
                    .baseMonth(null)
                    .weekendEffectScore(0.0)
                    .holidayEffectScore(0.0)
                    .bestDayOfWeek(null)
                    .bestHourRange("데이터 없음")
                    .build();
        }

        return results.get(0);
    }
}