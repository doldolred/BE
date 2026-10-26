package com.doldol.api.cart;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.member.Member;
import com.doldol.api.member.MemberRepository;
import com.doldol.api.product.ProductOption;
import com.doldol.api.product.ProductOptionRepository;
import com.doldol.api.shipping.ShippingPolicy;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductOptionRepository productOptionRepository;
    private final MemberRepository memberRepository;
    private final ShippingPolicy shippingPolicy;
    public CartResponse getCart(Long memberId) {
        List<CartResponse.Item> items = CartResponse.items(cartItemRepository.findAllWithProductByMemberId(memberId));
        int itemsPrice = items.stream().mapToInt(CartResponse.Item::lineTotal).sum();
        return CartResponse.of(items, shippingPolicy.quote(itemsPrice, null));
    }
    @Transactional
    public CartResponse addItem(Long memberId, AddCartItemRequest request) {
        ProductOption option = productOptionRepository.findWithProductById(request.optionId()).filter(ProductOption::isOnSale).orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        CartItem existing = cartItemRepository.findByMemberIdAndOptionId(memberId, option.getId()).orElse(null);
        int newQuantity = (existing == null ? 0 : existing.getQuantity()) + request.quantity();
        checkStock(option, newQuantity);
        if (existing == null) {Member member = memberRepository.findById(memberId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));cartItemRepository.save(CartItem.create(member, option, request.quantity()));
        } else {existing.addQuantity(request.quantity());
        }return getCart(memberId);
    }
    @Transactional
    public CartResponse updateQuantity(Long memberId, Long itemId, UpdateCartItemRequest request) {
        CartItem item = findMine(memberId, itemId);
        checkStock(item.getOption(), request.quantity());
        item.changeQuantity(request.quantity());
        return getCart(memberId);
    }
    @Transactional
    public CartResponse removeItem(Long memberId, Long itemId) {cartItemRepository.delete(findMine(memberId, itemId));
        return getCart(memberId);
    }
    private CartItem findMine(Long memberId, Long itemId) {return cartItemRepository.findByIdAndMemberId(itemId, memberId).orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
    }
    private void checkStock(ProductOption option, int quantity) {if (quantity > option.getStock()) {throw new BusinessException(ErrorCode.OUT_OF_STOCK, option.isSoldOut() ? "품절된 옵션입니다: " + option.getName() : "재고가 부족합니다. (남은 수량 " + option.getStock() + "개)");
    }}

}
