package com.example.demo.dto.myPage;

import com.example.demo.entity.myPage.BrandPlatform;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SnsAccountResponse {

    private Long brandPlatformId;
    private String platformCode;
    private String platformName;
    private String brandColor;
    private String channelName;
    private String channelUrl;
    private Boolean isConnected;
    private String tokenStatus;
    private LocalDateTime tokenExpiresAt;
    private LocalDateTime connectedAt;

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

    public static SnsAccountResponse notConnected(String platformCode, String platformName) {
        SnsAccountResponse r = new SnsAccountResponse();
        r.platformCode = platformCode;
        r.platformName = platformName;
        r.isConnected  = false;
        r.tokenStatus  = "NONE";
        return r;
    }

    private SnsAccountResponse() {}
}