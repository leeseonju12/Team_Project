package com.example.demo.weather2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Service
public class AsosDailyService {

    private static final String ENDPOINT =
            "http://apis.data.go.kr/1360000/AsosDalyInfoService/getWthrDataList";

    @Value("${asos.service-key}")
    private String serviceKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AsosDailyService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 일자료 조회
     *
     * @param startDt 시작일 (yyyyMMdd)
     * @param endDt   종료일 (yyyyMMdd)
     * @param stnIds  기상 관측소 지점 번호 (예: 108 = 서울)
     */
    public List<AsosDailyItem> getDailyData(String startDt, String endDt, String stnIds) {
        try {
            String url = ENDPOINT
                    + "?serviceKey=" + serviceKey
                    + "&pageNo=1"
                    + "&numOfRows=999"
                    + "&dataType=JSON"
                    + "&dataCd=ASOS"
                    + "&dateCd=DAY"
                    + "&startDt=" + startDt
                    + "&endDt=" + endDt
                    + "&stnIds=" + stnIds;

            String json = restTemplate.getForObject(url, String.class);

            AsosApiResponse<AsosDailyItem> apiResponse = objectMapper.readValue(
                    json, new TypeReference<AsosApiResponse<AsosDailyItem>>() {}
            );

            if (apiResponse.getResponse() == null
                    || !apiResponse.getResponse().getHeader().isSuccess()
                    || apiResponse.getResponse().getBody().getItems() == null) {
                return Collections.emptyList();
            }

            List<AsosDailyItem> items = apiResponse.getResponse().getBody().getItems().getItem();
            return items != null ? items : Collections.emptyList();

        } catch (Exception e) {
            throw new RuntimeException("ASOS 일자료 조회 실패: " + e.getMessage(), e);
        }
    }
}
