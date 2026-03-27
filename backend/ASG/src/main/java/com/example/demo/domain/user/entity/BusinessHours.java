package com.example.demo.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
 
/**
 * 요일별 영업시간 엔티티
 * User와 1:7 관계 (월~일 각각 1행)
 */
@Entity
@Table(name = "business_hours")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BusinessHours {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    /**
     * 연관된 사용자
     * CascadeType.ALL + orphanRemoval → User 삭제 시 함께 삭제
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
 
    /**
     * 요일 (0=월, 1=화, 2=수, 3=목, 4=금, 5=토, 6=일)
     */
    @Column(name = "day_of_week", nullable = false)
    private int dayOfWeek;
 
    /**
     * 영업 여부 (false → openTime/closeTime 무시)
     */
    @Column(name = "is_open", nullable = false)
    private boolean open;
 
    /**
     * 영업 시작 시간 (HH:mm 형식, ex: "09:00")
     * open=false 일 때 null 허용
     */
    @Column(name = "open_time", length = 5)
    private String openTime;
 
    /**
     * 영업 종료 시간 (HH:mm 형식, ex: "21:00")
     * open=false 일 때 null 허용
     */
    @Column(name = "close_time", length = 5)
    private String closeTime;
 
    // ── 비즈니스 메서드 ──────────────────────────────────────
 
    /**
     * 정적 팩토리: 영업일
     */
    public static BusinessHours openDay(User user, int dayOfWeek, String openTime, String closeTime) {
        return BusinessHours.builder()
                .user(user)
                .dayOfWeek(dayOfWeek)
                .open(true)
                .openTime(openTime)
                .closeTime(closeTime)
                .build();
    }
 
    /**
     * 정적 팩토리: 휴무일
     */
    public static BusinessHours closedDay(User user, int dayOfWeek) {
        return BusinessHours.builder()
                .user(user)
                .dayOfWeek(dayOfWeek)
                .open(false)
                .openTime(null)
                .closeTime(null)
                .build();
    }
 
    /**
     * 영업시간 업데이트 (기존 row 재사용)
     */
    public void update(boolean open, String openTime, String closeTime) {
        this.open = open;
        this.openTime = open ? openTime : null;
        this.closeTime = open ? closeTime : null;
    }
}