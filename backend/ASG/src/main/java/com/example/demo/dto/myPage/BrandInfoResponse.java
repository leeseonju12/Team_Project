package com.example.demo.dto.myPage;

import com.example.demo.entity.myPage.Brand;
import com.example.demo.entity.myPage.BrandOperationProfile;
import lombok.Getter;

import java.time.LocalTime;

@Getter
public class BrandInfoResponse {

    private final Long brandId;
    private final String brandName;
    private final String serviceName;
    private final String industryType;
    private final String locationName;
    private final String address;
    private final String phone;
    private final String profileImageUrl;

    // 영업시간
    private final LocalTime openTime;
    private final LocalTime closeTime;
    private final Byte regularClosedWeekday;

    public BrandInfoResponse(Brand brand, BrandOperationProfile profile) {
        this.brandId          = brand.getBrandId();
        this.brandName        = brand.getBrandName();
        this.serviceName      = brand.getServiceName();
        this.industryType     = brand.getIndustryType() != null ? brand.getIndustryType().getDescription() : null;
        this.locationName     = brand.getLocationName();
        this.address          = brand.getAddress();
        this.phone            = brand.getPhone();
        this.profileImageUrl  = brand.getProfileImageUrl();

        if (profile != null) {
            this.openTime             = profile.getOpenTime();
            this.closeTime            = profile.getCloseTime();
            this.regularClosedWeekday = profile.getRegularClosedWeekday();
        } else {
            this.openTime             = null;
            this.closeTime            = null;
            this.regularClosedWeekday = null;
        }
    }
}
