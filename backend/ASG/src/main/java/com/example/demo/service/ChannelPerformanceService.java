package com.example.demo.service;
import com.example.demo.dto.channel.ChannelPerformanceInsightDto;
import com.example.demo.dto.channel.ChannelPerformanceResponseDto;
import com.example.demo.dto.channel.ChannelPerformanceSummaryDto;
import com.example.demo.dto.channel.ChannelPerformanceTrendPointDto;
import com.example.demo.dto.channel.ChannelTopPostDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        int toDateKey = Integer.parseInt(toDate.format(DATE_KEY_FORMATTER));

        return ChannelPerformanceResponseDto.builder()
                .brandId(brandId)
                .from(fromDate.toString())
                .to(toDate.toString())
                .summaries(findChannelSummaries(brandId, fromDateKey, toDateKey))
                .trends(findDailyTrends(brandId, fromDateKey, toDateKey))
                .topPosts(findTopPosts(brandId, fromDateKey, toDateKey))
                .insight(findLatestInsight(brandId))
                .build();
    }

    private List<ChannelPerformanceSummaryDto> findChannelSummaries(Long brandId, int fromDateKey, int toDateKey) {
        String sql = """
                SELECT bp.brand_platform_id,
                       p.platform_code,
                       p.platform_name,
                       COALESCE(SUM(pmd.total_views), 0) AS total_views,
                       COALESCE(SUM(pmd.total_likes), 0) AS total_likes,
                       COALESCE(SUM(pmd.total_comments), 0) AS total_comments,
                       COALESCE(SUM(pmd.total_shares), 0) AS total_shares,
                       COALESCE(SUM(pmd.follower_growth), 0) AS follower_growth,
                       CASE
                           WHEN COALESCE(SUM(pmd.total_views), 0) = 0 THEN 0
                           ELSE (COALESCE(SUM(pmd.total_likes + pmd.total_comments + pmd.total_shares), 0)
                                / SUM(pmd.total_views)) * 100
                       END AS engagement_rate
                FROM brand_platform bp
                JOIN platform p ON p.platform_id = bp.platform_id
                LEFT JOIN platform_metric_daily pmd
                       ON pmd.brand_platform_id = bp.brand_platform_id
                      AND pmd.date_key BETWEEN ? AND ?
                WHERE bp.brand_id = ?
                GROUP BY bp.brand_platform_id, p.platform_code, p.platform_name
                ORDER BY total_views DESC
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> ChannelPerformanceSummaryDto.builder()
                        .brandPlatformId(rs.getLong("brand_platform_id"))
                        .platformCode(rs.getString("platform_code"))
                        .platformName(rs.getString("platform_name"))
                        .totalViews(rs.getInt("total_views"))
                        .totalLikes(rs.getInt("total_likes"))
                        .totalComments(rs.getInt("total_comments"))
                        .totalShares(rs.getInt("total_shares"))
                        .followerGrowth(rs.getInt("follower_growth"))
                        .engagementRate(rs.getDouble("engagement_rate"))
                        .build(),
                fromDateKey, toDateKey, brandId);
    }

    private List<ChannelPerformanceTrendPointDto> findDailyTrends(Long brandId, int fromDateKey, int toDateKey) {
        String sql = """
                SELECT pmd.date_key,
                       COALESCE(SUM(pmd.total_views), 0) AS views,
                       COALESCE(SUM(pmd.total_likes), 0) AS likes,
                       COALESCE(SUM(pmd.total_comments), 0) AS comments,
                       COALESCE(SUM(pmd.total_shares), 0) AS shares
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
                        .views(rs.getInt("views"))
                        .likes(rs.getInt("likes"))
                        .comments(rs.getInt("comments"))
                        .shares(rs.getInt("shares"))
                        .build(),
                brandId, fromDateKey, toDateKey);
    }

    private List<ChannelTopPostDto> findTopPosts(Long brandId, int fromDateKey, int toDateKey) {
        String sql = """
                SELECT cp.post_id,
                       COALESCE(cp.post_title, CONCAT('Post #', cp.post_id)) AS post_title,
                       p.platform_name,
                       COALESCE(SUM(pmd.view_count), 0) AS views,
                       COALESCE(SUM(pmd.like_count), 0) AS likes,
                       COALESCE(SUM(pmd.comment_count), 0) AS comments,
                       COALESCE(SUM(pmd.share_count), 0) AS shares,
                       CASE
                           WHEN COALESCE(SUM(pmd.view_count), 0) = 0 THEN 0
                           ELSE (COALESCE(SUM(pmd.like_count + pmd.comment_count + pmd.share_count), 0)
                               / SUM(pmd.view_count)) * 100
                       END AS engagement_rate
                FROM content_post cp
                JOIN brand_platform bp ON bp.brand_platform_id = cp.brand_platform_id
                JOIN platform p ON p.platform_id = bp.platform_id
                LEFT JOIN post_metric_daily pmd
                       ON pmd.post_id = cp.post_id
                      AND pmd.date_key BETWEEN ? AND ?
                WHERE bp.brand_id = ?
                GROUP BY cp.post_id, cp.post_title, p.platform_name
                ORDER BY engagement_rate DESC, views DESC
                LIMIT 5
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> ChannelTopPostDto.builder()
                        .postId(rs.getLong("post_id"))
                        .postTitle(rs.getString("post_title"))
                        .platformName(rs.getString("platform_name"))
                        .views(rs.getInt("views"))
                        .likes(rs.getInt("likes"))
                        .comments(rs.getInt("comments"))
                        .shares(rs.getInt("shares"))
                        .engagementRate(rs.getDouble("engagement_rate"))
                        .build(),
                fromDateKey, toDateKey, brandId);
    }

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
                        .weekendEffectScore(rs.getObject("weekend_effect_score") != null ? rs.getDouble("weekend_effect_score") : null)
                        .holidayEffectScore(rs.getObject("holiday_effect_score") != null ? rs.getDouble("holiday_effect_score") : null)
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