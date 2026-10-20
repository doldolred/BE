package com.doldol.api.cart;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.member.Member;
import com.doldol.api.member.MemberRepository;
import com.doldol.api.product.Product;
import com.doldol.api.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    public CartResponse getCart(Long memberId) {return CartResponse.from(cartItemRepository.findAllWithProductByMemberId(memberId));}
    @Transactional
    public CartResponse addItem(Long memberId, AddCartItemRequest request) {Product product = productRepository.findById(request.productId()).filter(Product::isActive).orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        CartItem existing = cartItemRepository.findByMemberIdAndProductId(memberId, product.getId()).orElse(null);
        int newQuantity = (existing == null ? 0 : existing.getQuantity()) + request.quantity();
        checkStock(product, newQuantity);
        if (existing == null) {Member member = memberRepository.findById(memberId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));cartItemRepository.save(CartItem.create(member, product, request.quantity()));
        } else {existing.addQuantity(request.quantity());
        }return getCart(memberId);
    }
    @Transactional
    public CartResponse updateQuantity(Long memberId, Long itemId, UpdateCartItemRequest request) {
        CartItem item = findMine(memberId, itemId);
        checkStock(item.getProduct(), request.quantity());
        item.changeQuantity(request.quantity());
        return getCart(memberId);
    }
    @Transactional
    public CartResponse removeItem(Long memberId, Long itemId) {cartItemRepository.delete(findMine(memberId, itemId));
        return getCart(memberId);
    }
    private CartItem findMine(Long memberId, Long itemId) {return cartItemRepository.findByIdAndMemberId(itemId, memberId).orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
    }
    private void checkStock(Product product, int quantity) {if (quantity > product.getStock()) {throw new BusinessException(ErrorCode.OUT_OF_STOCK, "재고가 부족합니다. (남은 수량 " + product.getStock() + "개)");
    }}

}