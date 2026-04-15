package com.example.demo.dto.myPage;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class BrandInfoRequest {

    private String brandName;
    private String serviceName;
    private String industryType;
    private String locationName;
    private String address;
    private String phone;

    // 영업시간
    private LocalTime openTime;
    private LocalTime closeTime;
    private Byte regularClosedWeekday;
}
