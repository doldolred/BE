package com.doldol.api.banner;

import com.doldol.api.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 띠배너 — 쇼핑몰 맨 위 한 줄 문구. 기간(startsAt ~ endsAt)을 비우면 제한 없음 */
@Entity
@Table(name = "banner")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Banner extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 80)
    private String message;
    @Column(length = 300)
    private String linkUrl;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private BannerTheme theme;
    private Instant startsAt;
    private Instant endsAt;
    @Column(nullable = false)
    private boolean active;
    @Column(nullable = false)
    private int sortOrder;
    public static Banner create(String message, String linkUrl, BannerTheme theme, Instant startsAt, Instant endsAt, boolean active, int sortOrder) {
        Banner banner = new Banner();
        banner.update(message, linkUrl, theme, startsAt, endsAt, active, sortOrder);
        return banner;
    }
    public void update(String message, String linkUrl, BannerTheme theme, Instant startsAt, Instant endsAt, boolean active, int sortOrder) {
        this.message = message.trim();
        this.linkUrl = linkUrl == null || linkUrl.isBlank() ? null : linkUrl.trim();
        this.theme = theme;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.active = active;
        this.sortOrder = sortOrder;
    }

}