package com.doldol.api.shipping;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 주문서에서 우편번호를 고르면 배송비를 미리 보여 준다 (로그인 없이) */
@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingPolicy shippingPolicy;

    @GetMapping("/quote")
    public ShippingQuote quote(@RequestParam @Min(0) int itemsPrice,
            @RequestParam(required = false) @Pattern(regexp = "^\\d{5}$", message = "우편번호는 숫자 5자리입니다.") String zipcode) {
        return shippingPolicy.quote(itemsPrice, zipcode);
    }

}
