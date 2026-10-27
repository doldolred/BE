package com.doldol.api.inquiry;

import java.time.Instant;

public record InquiryResponse(Long id, InquiryCategory category, String title, String content, String orderNumber, InquiryStatus status,
                              String answer, Instant answeredAt, Instant createdAt) {

    public static InquiryResponse from(Inquiry i) {return new InquiryResponse(i.getId(), i.getCategory(), i.getTitle(), i.getContent(), i.getOrderNumber(), i.getStatus(), i.getAnswer(), i.getAnsweredAt(), i.getCreatedAt());
    }
}