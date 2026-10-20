package com.doldol.api.cart;

import com.doldol.api.security.LoginMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    @GetMapping
    public CartResponse getCart(@AuthenticationPrincipal LoginMember loginMember) {return cartService.getCart(loginMember.id());}

    @PostMapping("/items")
    public CartResponse addItem(@AuthenticationPrincipal LoginMember loginMember, @Valid @RequestBody AddCartItemRequest request) {return cartService.addItem(loginMember.id(), request);
    }
    @PatchMapping("/items/{itemId}")
    public CartResponse updateQuantity(@AuthenticationPrincipal LoginMember loginMember, @PathVariable Long itemId, @Valid @RequestBody UpdateCartItemRequest request) {return cartService.updateQuantity(loginMember.id(), itemId, request);
    }
    @DeleteMapping("/items/{itemId}")
    public CartResponse removeItem(@AuthenticationPrincipal LoginMember loginMember, @PathVariable Long itemId) {return cartService.removeItem(loginMember.id(), itemId);
    }

}