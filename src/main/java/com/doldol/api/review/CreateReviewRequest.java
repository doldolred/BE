package com.doldol.api.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateReviewRequest(
        @NotNull(message = "후기를 쓸 주문 상품을 골라 주세요.")
        Long orderItemId,
        @NotNull(message = "별점을 골라 주세요.")
        @Min(value = 1, message = "별점은 1 ~ 5점이에요.")
        @Max(value = 5, message = "별점은 1 ~ 5점이에요.")
        Integer rating,
        @NotBlank(message = "후기를 입력해 주세요.")
        @Size(min = 10, max = 1_000, message = "후기는 10 ~ 1,000자로 입력해 주세요.")
        String content,
        // 선택. POST /api/reviews/images 로 올린 주소만 (최대 3장)
        @Size(max = 3, message = "사진은 3장까지 올릴 수 있어요.")
        List<@NotNull @Pattern(regexp = "^/uploads/reviews/\\d{6}/[0-9a-f-]{36}\\.(jpg|png|gif|webp)$", message = "올린 사진만 붙일 수 있어요.") String> imageUrls) {

}