package com.example.demo.service.myPage;

import com.example.demo.dto.myPage.BrandInfoRequest;
import com.example.demo.dto.myPage.BrandInfoResponse;
import com.example.demo.dto.myPage.ContentSettingsRequest;
import com.example.demo.dto.myPage.ContentSettingsResponse;
import com.example.demo.dto.myPage.SnsAccountResponse;
import com.example.demo.entity.myPage.Brand;
import com.example.demo.entity.myPage.BrandOperationProfile;
import com.example.demo.entity.myPage.BrandPlatform;
import com.example.demo.domain.ContentSettings;
import com.example.demo.repository.myPage.BrandOperationProfileRepository;
import com.example.demo.repository.myPage.BrandPlatformRepository;
import com.example.demo.repository.myPage.BrandRepository;
import com.example.demo.repository.myPage.ContentSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MypageService {

    // 로그인 연동 전까지 brandId 고정
    private static final Long BRAND_ID = 1L;

    private final BrandRepository brandRepository;
    private final BrandOperationProfileRepository operationProfileRepository;
    private final BrandPlatformRepository brandPlatformRepository;
    private final ContentSettingsRepository contentSettingsRepository;

    // ── 가게 정보 조회 ──────────────────────────────────────
    @Transactional(readOnly = true)
    public BrandInfoResponse getBrandInfo() {
        Brand brand = brandRepository.findById(BRAND_ID)
                .orElseThrow(() -> new IllegalArgumentException("브랜드를 찾을 수 없습니다."));

        BrandOperationProfile profile = operationProfileRepository
                .findByBrand_BrandId(BRAND_ID)
                .orElse(null);

        return new BrandInfoResponse(brand, profile);
    }

    // ── 가게 정보 수정 ──────────────────────────────────────
    @Transactional
    public void updateBrandInfo(BrandInfoRequest request) {
        Brand brand = brandRepository.findById(BRAND_ID)
                .orElseThrow(() -> new IllegalArgumentException("브랜드를 찾을 수 없습니다."));

        brand.setBrandName(request.getBrandName());
        brand.setServiceName(request.getServiceName());
        brand.setIndustryType(request.getIndustryType());
        brand.setLocationName(request.getLocationName());
        brand.setAddress(request.getAddress());
        brand.setPhone(request.getPhone());

        // 영업시간 저장 또는 업데이트
        BrandOperationProfile profile = operationProfileRepository
                .findByBrand_BrandId(BRAND_ID)
                .orElse(new BrandOperationProfile());

        profile.setBrand(brand);
        profile.setOpenTime(request.getOpenTime());
        profile.setCloseTime(request.getCloseTime());
        profile.setRegularClosedWeekday(request.getRegularClosedWeekday());

        operationProfileRepository.save(profile);
    }

    // ── 콘텐츠 설정 조회 ────────────────────────────────────
    /*@Transactional(readOnly = true)
    public ContentSettingsResponse getContentSettings() {
        return contentSettingsRepository.findById(BRAND_ID)
                .map(ContentSettingsResponse::new)
                .orElse(null);
    }*/

    // ── 콘텐츠 설정 수정 ────────────────────────────────────
    /*@Transactional
    public void updateContentSettings(ContentSettingsRequest request) {
        ContentSettings settings = contentSettingsRepository.findById(BRAND_ID)
                .orElseThrow(() -> new IllegalArgumentException("콘텐츠 설정을 찾을 수 없습니다."));

        settings.update(
                request.getIntroTemplate(),
                request.getOutroTemplate(),
                request.getTone(),
                request.getEmojiLevel(),
                request.getTargetLength()
        );
    }*/

    // ── SNS 연동 목록 조회 ──────────────────────────────────
    @Transactional(readOnly = true)
    public List<SnsAccountResponse> getSnsAccounts() {
        List<BrandPlatform> list = brandPlatformRepository.findByBrand_BrandId(BRAND_ID);
        return list.stream()
                .map(SnsAccountResponse::new)
                .collect(Collectors.toList());
    }

    // ── SNS 연동 해제 ───────────────────────────────────────
    @Transactional
    public void disconnectSns(Long brandPlatformId) {
        BrandPlatform bp = brandPlatformRepository.findById(brandPlatformId)
                .orElseThrow(() -> new IllegalArgumentException("연동 정보를 찾을 수 없습니다."));

        bp.setIsConnected(false);
        bp.setAccessToken(null);
        bp.setRefreshToken(null);
        bp.setTokenStatus("EXPIRED");
    }
}
