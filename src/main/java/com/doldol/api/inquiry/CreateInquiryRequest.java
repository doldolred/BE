package com.doldol.api.inquiry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateInquiryRequest(
        @NotNull(message = "문의 종류를 골라 주세요.")
        InquiryCategory category,
        @NotBlank(message = "제목을 입력해 주세요.")
        @Size(max = 100, message = "제목은 100자 이하로 입력해 주세요.")
        String title,
        @NotBlank(message = "내용을 입력해 주세요.")
        @Size(max = 5_000, message = "내용은 5,000자 이하로 입력해 주세요.")
        String content,
        @Size(max = 30, message = "주문번호가 너무 깁니다.")
        String orderNumber) {
}
