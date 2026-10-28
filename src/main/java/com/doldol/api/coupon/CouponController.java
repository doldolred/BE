package com.doldol.api.coupon;

import com.doldol.api.security.LoginMember;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;
    @GetMapping("/check")
    public CouponCheckResponse check(@AuthenticationPrincipal LoginMember loginMember, @RequestParam String code){
        return couponService.check(loginMember.id(), code);
    }
}
