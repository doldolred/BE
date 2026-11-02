package com.doldol.api.order;

import com.doldol.api.security.LoginMember;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final PurchaseConfirmService purchaseConfirmService;
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDetailResponse createOrder(@AuthenticationPrincipal LoginMember loginMember, @Valid @RequestBody CreateOrderRequest request){
        return orderService.createOrder(loginMember.id(), request);
    }
    @GetMapping
    public List<OrderSummaryResponse> getOrders(@AuthenticationPrincipal LoginMember loginMember){
        return orderService.getOrders(loginMember.id());
    }
    @GetMapping("/{orderNumber}")
    public OrderDetailResponse getOrder(@AuthenticationPrincipal LoginMember loginMember, @PathVariable String orderNumber){
        return orderService.getOrder(loginMember.id(), orderNumber);
    }
    /** 구매확정 (V15) — 적립금이 들어오고, 이후 반품 · 교환은 신청할 수 없다 */
    @PostMapping("/{orderNumber}/confirm")
    public OrderDetailResponse confirm(@AuthenticationPrincipal LoginMember loginMember, @PathVariable String orderNumber){
        return purchaseConfirmService.confirm(loginMember.id(), orderNumber);
    }
    @PostMapping("/{orderNumber}/cancel")
    public OrderDetailResponse cancelOrder(@AuthenticationPrincipal LoginMember loginMember, @PathVariable String orderNumber){
        return orderService.cancelOrder(loginMember.id(), orderNumber);
    }
}
