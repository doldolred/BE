package com.doldol.api.admin.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

/** 상품 등록 · 수정. 옵션은 전부 보낸다 — id 가 있으면 수정, 없으면 새로, 목록에서 빠진 옵션은 판매중지 */
public record AdminProductRequest(
        @NotBlank(message = "상품명을 입력해 주세요.")
        @Size(max = 100, message = "상품명은 100자 이하로 입력해 주세요.")
        String name,
        @Size(max = 2000, message = "설명은 2000자 이하로 입력해 주세요.")
        String description,
        @Size(max = 500, message = "이미지 주소가 너무 깁니다.")
        String imageUrl,
        @NotNull(message = "판매 여부를 선택해 주세요.")
        Boolean active,
        @NotEmpty(message = "옵션을 하나 이상 넣어 주세요.")
        @Size(max = 20, message = "옵션은 20개까지 넣을 수 있습니다.")
        List<@Valid OptionRequest> options
) {

    public record OptionRequest(
            Long id,
            @NotBlank(message = "옵션 이름을 입력해 주세요.")
            @Size(max = 50, message = "옵션 이름은 50자 이하로 입력해 주세요.")
            String name,
            @NotNull(message = "정상가를 입력해 주세요.")
            @Min(value = 0, message = "정상가는 0원 이상이어야 합니다.")
            @Max(value = 10_000_000, message = "정상가가 너무 큽니다.")
            Integer listPrice,
            @NotNull(message = "판매가를 입력해 주세요.")
            @Min(value = 0, message = "판매가는 0원 이상이어야 합니다.")
            @Max(value = 10_000_000, message = "판매가가 너무 큽니다.")
            Integer price,
            @NotNull(message = "재고를 입력해 주세요.")
            @Min(value = 0, message = "재고는 0개 이상이어야 합니다.")
            Integer stock,
            @NotNull(message = "옵션 판매 여부를 선택해 주세요.")
            Boolean active
    ) {
    }

}
