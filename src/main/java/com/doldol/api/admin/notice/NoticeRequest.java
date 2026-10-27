package com.doldol.api.admin.notice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NoticeRequest(
        @NotBlank(message = "제목을 입력해 주세요.")
        @Size(max = 100, message = "제목은 100자 이하로 입력해 주세요.")
        String title,
        @NotBlank(message = "내용을 입력해 주세요.")
        @Size(max = 20_000, message = "내용은 20,000자 이하로 입력해 주세요.")
        String content,
        @NotNull(message = "고정 여부를 골라 주세요.")
        Boolean pinned,
        @NotNull(message = "공개 여부를 골라 주세요.")
        Boolean published) {

}