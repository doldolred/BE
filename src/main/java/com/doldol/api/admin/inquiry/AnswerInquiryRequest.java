package com.doldol.api.admin.inquiry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnswerInquiryRequest(
        @NotBlank(message = "답변을 입력해 주세요.")
        @Size(max = 5_000, message = "답변은 5,000자 이하로 입력해 주세요.")
        String answer) {}
