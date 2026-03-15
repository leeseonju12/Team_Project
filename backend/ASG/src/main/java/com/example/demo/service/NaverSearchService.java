package com.example.demo.service;

import com.example.demo.dto.channel.NaverKeywordDto;
import com.example.demo.dto.channel.NaverSearchResponseDto;
import com.example.demo.dto.channel.NaverSearchSummaryDto;
import com.example.demo.dto.channel.NaverSearchTrendDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NaverSearchService {

    private final JdbcTemplate jdbcTemplate;
    private static final DateTimeFormatter DATE_KEY_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * period: "week" | "month" | "year"
     */
    public NaverSearchResponseDto getDashboard(Long brandId, LocalDate from, LocalDate to, String period) {

        if (from.isAfter(to)) { LocalDate t = from; from = to; to = t; }

        int fromKey     = toDateKey(from);
        int toKey       = toDateKey(to);
        long days       = to.toEpochDay() - from.toEpochDay() + 1;
        int prevToKey   = toDateKey(from.minusDays(1));
        int prevFromKey = toDateKey(from.minusDays(days));

        return NaverSearchResponseDto.builder()
                .summary(buildSummary(brandId, fromKey, toKey, prevFromKey, prevToKey))
                .trends(findTrends(brandId, fromKey, toKey, period))
                .keywords(findTopKeywords(brandId, fromKey, toKey))
                .build();
    }

    // ── 요약 ─────────────────────────────────────────────────────────
    private NaverSearchSummaryDto buildSummary(Long brandId,
                                               int fromKey, int toKey,
                                               int prevFromKey, int prevToKey) {
        String sql = """
                SELECT COALESCE(SUM(search_count), 0) AS s,
                       COALESCE(SUM(click_count),  0) AS c
                FROM search_performance_daily
                WHERE brand_id = ? AND search_engine = 'naver'
                  AND date_key BETWEEN ? AND ?
                """;
        int[] cur  = jdbcTemplate.queryForObject(sql,
                (rs, rn) -> new int[]{rs.getInt("s"), rs.getInt("c")},
                brandId, fromKey, toKey);
        int[] prev = jdbcTemplate.queryForObject(sql,
                (rs, rn) -> new int[]{rs.getInt("s"), rs.getInt("c")},
                brandId, prevFromKey, prevToKey);
        return NaverSearchSummaryDto.builder()
                .totalSearchCount(cur[0])
                .totalClickCount(cur[1])
                .searchGrowthPct(calcGrowth(cur[0], prev[0]))
                .clickGrowthPct(calcGrowth(cur[1], prev[1]))
                .build();
    }

    // ── 트렌드: 기간별 집계 ──────────────────────────────────────────
    // week  → 일별  (최대 7개)
    // month → 주별  (4~5개)
    // year  → 월별  (최대 12개)
    private List<NaverSearchTrendDto> findTrends(Long brandId, int fromKey, int toKey, String period) {

        String sql;

        if ("week".equals(period)) {
            sql = """
                    SELECT date_key,
                           COALESCE(SUM(search_count), 0) AS search_count,
                           COALESCE(SUM(click_count),  0) AS click_count
                    FROM search_performance_daily
                    WHERE brand_id = ? AND search_engine = 'naver'
                      AND date_key BETWEEN ? AND ?
                    GROUP BY date_key
                    ORDER BY date_key
                    """;
        } else if ("month".equals(period)) {
            // 주별 집계 — 해당 주의 마지막 date_key를 대표값으로
            sql = """
                    SELECT MAX(date_key)                    AS date_key,
                           COALESCE(SUM(search_count), 0)  AS search_count,
                           COALESCE(SUM(click_count),  0)  AS click_count
                    FROM search_performance_daily
                    WHERE brand_id = ? AND search_engine = 'naver'
                      AND date_key BETWEEN ? AND ?
                    GROUP BY YEARWEEK(STR_TO_DATE(date_key, '%Y%m%d'), 1)
                    ORDER BY date_key
                    """;
        } else {
            // 연간 — 월별 집계
            sql = """
                    SELECT MAX(date_key)                    AS date_key,
                           COALESCE(SUM(search_count), 0)  AS search_count,
                           COALESCE(SUM(click_count),  0)  AS click_count
                    FROM search_performance_daily
                    WHERE brand_id = ? AND search_engine = 'naver'
                      AND date_key BETWEEN ? AND ?
                    GROUP BY DATE_FORMAT(STR_TO_DATE(date_key, '%Y%m%d'), '%Y-%m')
                    ORDER BY date_key
                    """;
        }

        return jdbcTemplate.query(sql,
                (rs, rn) -> NaverSearchTrendDto.builder()
                        .dateKey(rs.getInt("date_key"))
                        .searchCount(rs.getInt("search_count"))
                        .clickCount(rs.getInt("click_count"))
                        .build(),
                brandId, fromKey, toKey);
    }

    // ── 인기 키워드 Top 7 ─────────────────────────────────────────────
    private List<NaverKeywordDto> findTopKeywords(Long brandId, int fromKey, int toKey) {
        String sql = """
                SELECT km.keyword_id,
                       km.keyword_text,
                       km.keyword_type,
                       COALESCE(SUM(kpd.search_count), 0) AS search_count,
                       COALESCE(SUM(kpd.click_count),  0) AS click_count,
                       ROW_NUMBER() OVER (ORDER BY SUM(kpd.search_count) DESC) AS rank_no
                FROM keyword_master km
                LEFT JOIN keyword_performance_daily kpd
                       ON kpd.keyword_id = km.keyword_id
                      AND kpd.date_key BETWEEN ? AND ?
                WHERE km.brand_id = ?
                GROUP BY km.keyword_id, km.keyword_text, km.keyword_type
                ORDER BY search_count DESC
                LIMIT 7
                """;
        return jdbcTemplate.query(sql,
                (rs, rn) -> NaverKeywordDto.builder()
                        .keywordId(rs.getLong("keyword_id"))
                        .keywordText(rs.getString("keyword_text"))
                        .keywordType(rs.getString("keyword_type"))
                        .searchCount(rs.getInt("search_count"))
                        .clickCount(rs.getInt("click_count"))
                        .rankNo((int) rs.getLong("rank_no"))
                        .build(),
                fromKey, toKey, brandId);
    }

    private int toDateKey(LocalDate d) {
        return Integer.parseInt(d.format(DATE_KEY_FMT));
    }

    private double calcGrowth(int cur, int prev) {
        if (prev == 0) return 0.0;
        return Math.round((cur - prev) / (double) prev * 1000.0) / 10.0;
    }
}