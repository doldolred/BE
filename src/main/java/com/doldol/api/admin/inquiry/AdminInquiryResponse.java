package com.doldol.api.admin.inquiry;

import com.doldol.api.inquiry.Inquiry;
import com.doldol.api.inquiry.InquiryCategory;
import com.doldol.api.inquiry.InquiryStatus;
import com.doldol.api.member.Member;
import java.time.Instant;

public record AdminInquiryResponse(Long id, InquiryCategory category, String title, String content, String orderNumber, InquiryStatus status, String answer, Instant answeredAt, Instant createdAt, Long memberId, String memberEmail, String memberName) {

    public static AdminInquiryResponse from(Inquiry i) {
        Member m = i.getMember();
        return new AdminInquiryResponse(i.getId(), i.getCategory(), i.getTitle(), i.getContent(), i.getOrderNumber(), i.getStatus(), i.getAnswer(), i.getAnsweredAt(), i.getCreatedAt(), m.getId(), m.getEmail(), m.getName());
    }

}