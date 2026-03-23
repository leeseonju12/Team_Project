package com.example.demo.dto.channel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SerpApiResponseDto {

    // 구글: related_searches
    @JsonProperty("related_searches")
    private List<GoogleRelatedSearch> relatedSearches;

    // 네이버: related_results
    @JsonProperty("related_results")
    private List<NaverRelatedResult> relatedResults;

    // ── 구글 연관검색어 ──
    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GoogleRelatedSearch {
        @JsonProperty("query")
        private String query;

        @JsonProperty("block_position")
        private Integer blockPosition;
    }

    // ── 네이버 연관검색어 ──
    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NaverRelatedResult {
        @JsonProperty("position")
        private Integer position;

        @JsonProperty("title")
        private String title;

        @JsonProperty("link")
        private String link;
    }
}