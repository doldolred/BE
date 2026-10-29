package com.doldol.api.admin.product;

import com.doldol.api.product.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * 상품 등록 · 수정. 옵션은 전부 보낸다 — id 가 있으면 수정, 없으면 새로, 목록에서 빠진 옵션은 판매중지.
 * 판매중이면 판매할 옵션이 하나 이상 있어야 한다 (출시 예정 · 비공개는 옵션 없이도 된다 — 서비스에서 확인).
 * 아래쪽은 라인업 항목 (메인 라인업 · 상세 연출)
 */
public record AdminProductRequest(
        @NotBlank(message = "상품명을 입력해 주세요.")
        @Size(max = 100, message = "상품명은 100자 이하로 입력해 주세요.")
        String name,
        @Size(max = 2000, message = "설명은 2000자 이하로 입력해 주세요.")
        String description,
        @Size(max = 500, message = "이미지 주소가 너무 깁니다.")
        String imageUrl,
        @NotNull(message = "상태를 선택해 주세요.")
        ProductStatus status,
        @Size(max = 20, message = "옵션은 20개까지 넣을 수 있습니다.")
        List<@Valid OptionRequest> options,
        @Size(max = 40, message = "영문 이름은 40자 이하로 입력해 주세요.")
        String enName,
        @Size(max = 100, message = "구성은 100자 이하로 입력해 주세요.")
        String spec,
        @Size(max = 4, message = "스티커 문구는 4개까지 넣을 수 있습니다.")
        List<@Size(max = 20, message = "스티커 문구는 20자 이하로 입력해 주세요.") String> points,
        @NotNull(message = "색을 선택해 주세요.")
        @Pattern(regexp = "NAVY|PINK|LIME", message = "색을 다시 선택해 주세요.")
        String theme,
        @NotNull(message = "그림을 선택해 주세요.")
        @Pattern(regexp = "POUCH|BOTTLE|JAR", message = "그림을 다시 선택해 주세요.")
        String art,
        @Size(max = 500, message = "도안 주소가 너무 깁니다.")
        String artFrontUrl,
        @Size(max = 500, message = "도안 주소가 너무 깁니다.")
        String artBackUrl,
        @NotNull(message = "라인업 순서를 입력해 주세요.")
        @Min(value = 0, message = "라인업 순서는 0 이상이어야 합니다.")
        @Max(value = 999, message = "라인업 순서는 999 이하로 입력해 주세요.")
        Integer lineupOrder
) {

    public List<OptionRequest> optionsOrEmpty() {
        return options == null ? List.of() : options;
    }

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
