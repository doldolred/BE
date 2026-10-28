package com.doldol.api.admin.review;

import jakarta.validation.constraints.Size;

/** 보낸 것만 바꾼다 (null = 그대로). reply 가 빈 글이면 답글 삭제 */
public record AdminReviewUpdateRequest(
        Boolean hidden,
        Boolean best,
        @Size(max = 1_000, message = "답글은 1,000자 이하로 입력해 주세요.")
        String reply) {

}