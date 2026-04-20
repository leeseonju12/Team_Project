package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "user_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "active_platforms", columnDefinition = "json")
    private List<String> activePlatforms;

    @Column(length = 50)
    private String toneStyle;

    @Column(length = 50)
    private String emojiLevel;

    @Column(nullable = false)
    private int maxLength;

    @Builder
    public UserSetting(Long userId, List<String> activePlatforms, String toneStyle, String emojiLevel, int maxLength) {
        this.userId = userId;
        this.activePlatforms = activePlatforms;
        this.toneStyle = toneStyle;
        this.emojiLevel = emojiLevel;
        this.maxLength = maxLength;
    }
}