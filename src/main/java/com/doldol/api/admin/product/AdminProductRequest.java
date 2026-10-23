package com.doldol.api.admin.product;

import jakarta.validation.constraints.*;

public record AdminProductRequest(
        @NotBlank(message = "상품명을 입력해 주세요.")
        @Size(max = 100, message = "상품명은 100자 이하로 입력해 주세요.")
        String name,
        @Size(max = 2000, message = "설명은 2000자 이하로 입력해 주세요.")
        String description,
        @NotNull(message = "가격을 입력해 주세요.")
        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        @Max(value = 10_000_000, message = "가격이 너무 큽니다.")
        Integer price,
        @NotNull(message = "재고를 입력해 주세요.")
        @Min(value = 0, message = "재고는 0개 이상이어야 합니다.")
        Integer stock,
        @Size(max = 500, message = "이미지 주소가 너무 깁니다.")
        String imageUrl,
        @NotNull(message = "판매 여부를 선택해 주세요.")
        Boolean active
) {
}
