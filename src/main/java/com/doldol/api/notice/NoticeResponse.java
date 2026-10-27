package com.doldol.api.notice;

import java.time.Instant;

public record NoticeResponse(Long id, String title, String content, boolean pinned, boolean published, Instant createdAt, Instant updatedAt){
    public static NoticeResponse from(Notice n){
        return new NoticeResponse(n.getId(), n.getTitle(), n.getContent(), n.isPinned(), n.isPublished(), n.getCreatedAt(), n.getUpdatedAt());
    }
}
