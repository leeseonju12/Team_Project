package com.example.demo.repository;

import com.example.demo.dto.channel.BrandSearchRequestDto;
import org.springframework.jdbc.core.JdbcTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BrandRepository {

    private final JdbcTemplate jdbcTemplate;

    public BrandSearchRequestDto findById(Long brandId) {
        String sql = "SELECT brand_id, brand_name, industry_type FROM brand WHERE brand_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                new BrandSearchRequestDto(
                    rs.getString("brand_name"),
                    null  // period는 여기선 불필요
                ),
                brandId
            );
        } catch (Exception e) {
            return new BrandSearchRequestDto("알 수 없는 브랜드", null);
        }
    }
}