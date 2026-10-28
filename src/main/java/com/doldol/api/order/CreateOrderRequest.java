package com.doldol.api.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(

        @NotBlank(message = "받는 분 이름을 입력해 주세요.")
        @Size(max = 50, message = "이름은 50자 이하로 입력해 주세요.")
        String receiverName,
        @NotBlank(message = "연락처를 입력해 주세요.")
        @Pattern(regexp = "^0\\d{1,2}-?\\d{3,4}-?\\d{4}$", message = "연락처 형식이 아닙니다.")
        String receiverPhone,
        @NotBlank(message = "우편번호를 입력해 주세요.")
        @Pattern(regexp = "^\\d{5}$", message = "우편번호는 숫자 5자리입니다.")
        String zipcode,
        @NotBlank(message = "주소를 입력해 주세요.")
        @Size(max = 255, message = "주소가 너무 깁니다.")
        String address1,
        @Size(max = 255, message = "상세주소가 너무 깁니다.")
        String address2,
        @Size(max = 255, message = "배송 메모는 255자 이하로 입력해 주세요.")
        String memo,
        // 선택 — 할인코드 (V11)
        @Size(max = 30, message = "할인코드가 너무 깁니다.")
        String couponCode) {}