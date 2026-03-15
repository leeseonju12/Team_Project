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
public class AsosHourlyService {

    private static final String ENDPOINT =
            "http://apis.data.go.kr/1360000/AsosHourlyInfoService/getWthrDataList";

    @Value("${asos.service-key}")
    private String serviceKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AsosHourlyService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 시간자료 조회
     *
     * @param startDt 시작일 (yyyyMMdd)
     * @param startHh 시작시 (HH, 00~23)
     * @param endDt   종료일 (yyyyMMdd)
     * @param endHh   종료시 (HH, 00~23)
     * @param stnIds  기상 관측소 지점 번호 (예: 108 = 서울)
     */
    public List<AsosHourlyItem> getHourlyData(
            String startDt, String startHh,
            String endDt, String endHh,
            String stnIds) {
        try {
            String url = ENDPOINT
                    + "?serviceKey=" + serviceKey
                    + "&pageNo=1"
                    + "&numOfRows=999"
                    + "&dataType=JSON"
                    + "&dataCd=ASOS"
                    + "&dateCd=HR"
                    + "&startDt=" + startDt
                    + "&startHh=" + startHh
                    + "&endDt=" + endDt
                    + "&endHh=" + endHh
                    + "&stnIds=" + stnIds;

            String json = restTemplate.getForObject(url, String.class);

            AsosApiResponse<AsosHourlyItem> apiResponse = objectMapper.readValue(
                    json, new TypeReference<AsosApiResponse<AsosHourlyItem>>() {}
            );

            if (apiResponse.getResponse() == null
                    || !apiResponse.getResponse().getHeader().isSuccess()
                    || apiResponse.getResponse().getBody().getItems() == null) {
                return Collections.emptyList();
            }

            List<AsosHourlyItem> items = apiResponse.getResponse().getBody().getItems().getItem();
            return items != null ? items : Collections.emptyList();

        } catch (Exception e) {
            throw new RuntimeException("ASOS 시간자료 조회 실패: " + e.getMessage(), e);
        }
    }
}
