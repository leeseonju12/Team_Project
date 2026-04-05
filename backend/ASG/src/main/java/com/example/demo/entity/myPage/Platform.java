package com.example.demo.entity.myPage;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "platform")
@Getter @Setter
@NoArgsConstructor
public class Platform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "platform_id")
    private Long platformId;

    @Column(name = "platform_code", unique = true, nullable = false, length = 30)
    private String platformCode; // instagram, facebook, naver, kakao

    @Column(name = "platform_name", nullable = false, length = 50)
    private String platformName;

    @Column(name = "brand_color", length = 20)
    private String brandColor;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
