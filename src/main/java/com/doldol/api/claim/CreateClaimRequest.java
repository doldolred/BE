package com.doldol.api.claim;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateClaimRequest(
        @NotNull(message = "취소 · 반품 · 교환 중 하나를 골라 주세요.")
        ClaimType type,
        @NotNull(message = "이유를 골라 주세요.")
        ClaimReason reason,
        @NotBlank(message = "자세한 내용을 입력해 주세요.")
        @Size(max = 2_000, message = "내용은 2,000자 이하로 입력해 주세요.")
        String detail,
        // 선택. POST /api/claims/images 로 올린 주소만 (최대 3장)
        @Size(max = 3, message = "사진은 3장까지 올릴 수 있어요.")
        List<@NotNull @Pattern(regexp = "^/uploads/claims/\\d{6}/[0-9a-f-]{36}\\.(jpg|png|gif|webp)$", message = "올린 사진만 붙일 수 있어요.") String> imageUrls) {

}