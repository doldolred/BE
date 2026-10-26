package com.doldol.api.admin.shipping;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ShippingAreaRequest(
        @NotBlank(message = "지역 이름을 입력해 주세요.")
        @Size(max = 50, message = "지역 이름은 50자 이하로 입력해 주세요.")
        String name,
        @NotBlank(message = "시작 우편번호를 입력해 주세요.")
        @Pattern(regexp = "^\\d{5}$", message = "우편번호는 숫자 5자리입니다.")
        String zipFrom,
        @NotBlank(message = "끝 우편번호를 입력해 주세요.")
        @Pattern(regexp = "^\\d{5}$", message = "우편번호는 숫자 5자리입니다.")
        String zipTo,
        @NotNull(message = "추가 배송비를 입력해 주세요.")
        @Min(value = 0, message = "추가 배송비는 0원 이상이어야 합니다.")
        @Max(value = 100_000, message = "추가 배송비가 너무 큽니다.")
        Integer extraFee) {

}
