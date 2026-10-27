package com.doldol.api.banner;

import java.time.Instant;

public record BannerResponse(Long id, String message, String linkUrl, BannerTheme theme, Instant startsAt, Instant endsAt, boolean active, int sortOrder) {
    public static BannerResponse from(Banner b) {
        return new BannerResponse(b.getId(), b.getMessage(), b.getLinkUrl(), b.getTheme(), b.getStartsAt(), b.getEndsAt(), b.isActive(), b.getSortOrder());
    }
}
