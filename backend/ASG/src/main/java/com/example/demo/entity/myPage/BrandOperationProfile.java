package com.example.demo.entity.myPage;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "brand_operation_profile")
@Getter @Setter
@NoArgsConstructor
public class BrandOperationProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "operation_profile_id")
    private Long operationProfileId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", unique = true, nullable = false)
    private Brand brand;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "regular_closed_weekday")
    private Byte regularClosedWeekday; // 1=월 ~ 7=일

    @Column(name = "weekend_impact_type", length = 20)
    private String weekendImpactType; // positive / neutral / negative

    @Column(name = "holiday_impact_type", length = 20)
    private String holidayImpactType;

    @Column(name = "peak_business_time", length = 50)
    private String peakBusinessTime;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
