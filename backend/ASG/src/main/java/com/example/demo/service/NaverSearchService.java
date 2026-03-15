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

    public NaverSearchResponseDto getDashboard(Long brandId, LocalDate from, LocalDate to) {

        // 날짜 역전 방지
        if (from.isAfter(to)) { LocalDate t = from; from = to; to = t; }

        int fromKey = toDateKey(from);
        int toKey   = toDateKey(to);

        // 전 기간 길이 동일하게 계산
        long days       = to.toEpochDay() - from.toEpochDay() + 1;
        int prevToKey   = toDateKey(from.minusDays(1));
        int prevFromKey = toDateKey(from.minusDays(days));

        NaverSearchSummaryDto summary  = buildSummary(brandId, fromKey, toKey, prevFromKey, prevToKey);
        List<NaverSearchTrendDto> trends   = findTrends(brandId, fromKey, toKey);
        List<NaverKeywordDto>     keywords = findTopKeywords(brandId, fromKey, toKey);

        return NaverSearchResponseDto.builder()
                .summary(summary)
                .trends(trends)
                .keywords(keywords)
                .build();
    }

    // ── 요약 (현재 기간 합계 + 전기간 대비 증감률) ────────────────────
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

        // 현재 기간
        int[] cur  = jdbcTemplate.queryForObject(sql,
                (rs, rn) -> new int[]{rs.getInt("s"), rs.getInt("c")},
                brandId, fromKey, toKey);

        // 이전 기간
        int[] prev = jdbcTemplate.queryForObject(sql,
                (rs, rn) -> new int[]{rs.getInt("s"), rs.getInt("c")},
                brandId, prevFromKey, prevToKey);

        double searchGrowth = calcGrowth(cur[0], prev[0]);
        double clickGrowth  = calcGrowth(cur[1], prev[1]);

        return NaverSearchSummaryDto.builder()
                .totalSearchCount(cur[0])
                .totalClickCount(cur[1])
                .searchGrowthPct(searchGrowth)
                .clickGrowthPct(clickGrowth)
                .build();
    }

    // ── 일별 트렌드 ───────────────────────────────────────────────────
    private List<NaverSearchTrendDto> findTrends(Long brandId, int fromKey, int toKey) {
        String sql = """
                SELECT date_key,
                       COALESCE(SUM(search_count), 0) AS search_count,
                       COALESCE(SUM(click_count),  0) AS click_count
                FROM search_performance_daily
                WHERE brand_id = ? AND search_engine = 'naver'
                  AND date_key BETWEEN ? AND ?
                GROUP BY date_key
                ORDER BY date_key
                """;

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

    // ── 유틸 ──────────────────────────────────────────────────────────
    private int toDateKey(LocalDate d) {
        return Integer.parseInt(d.format(DATE_KEY_FMT));
    }

    private double calcGrowth(int cur, int prev) {
        if (prev == 0) return 0.0;
        return Math.round((cur - prev) / (double) prev * 1000.0) / 10.0;
    }
}
