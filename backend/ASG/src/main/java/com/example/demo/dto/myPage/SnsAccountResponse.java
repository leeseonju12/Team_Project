package com.example.demo.dto.myPage;

import com.example.demo.entity.myPage.BrandPlatform;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SnsAccountResponse {

    private final Long brandPlatformId;
    private final String platformCode;   // instagram / facebook / naver / kakao
    private final String platformName;
    private final String brandColor;
    private final String channelName;
    private final String channelUrl;
    private final Boolean isConnected;
    private final String tokenStatus;    // ACTIVE / EXPIRED
    private final LocalDateTime tokenExpiresAt;
    private final LocalDateTime connectedAt;

    public SnsAccountResponse(BrandPlatform bp) {
        this.brandPlatformId = bp.getBrandPlatformId();
        this.platformCode    = bp.getPlatform().getPlatformCode();
        this.platformName    = bp.getPlatform().getPlatformName();
        this.brandColor      = bp.getPlatform().getBrandColor();
        this.channelName     = bp.getChannelName();
        this.channelUrl      = bp.getChannelUrl();
        this.isConnected     = bp.getIsConnected();
        this.tokenStatus     = bp.getTokenStatus();
        this.tokenExpiresAt  = bp.getTokenExpiresAt();
        this.connectedAt     = bp.getConnectedAt();
    }
}
