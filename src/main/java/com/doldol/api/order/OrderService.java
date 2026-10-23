package com.doldol.api.order;

import com.doldol.api.cart.CartItem;
import com.doldol.api.cart.CartItemRepository;
import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.member.Member;
import com.doldol.api.member.MemberRepository;
import com.doldol.api.payment.PaymentService;
import com.doldol.api.product.Product;
import com.doldol.api.product.ProductRepository;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private static final DateTimeFormatter ORDER_DATE = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.of("Asia/Seoul"));
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final PaymentService paymentService;

    @Transactional
    public OrderDetailResponse createOrder(Long memberId, CreateOrderRequest request) {
        List<CartItem> cartItems = cartItemRepository.findAllWithProductByMemberId(memberId);
        if (cartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        Order order = Order.create(generateOrderNumber(), member, request);
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (!product.isActive()) {throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "판매가 끝난 상품이 있어요: " + product.getName());}
            if (productRepository.decreaseStock(product.getId(), cartItem.getQuantity()) == 0) {throw new BusinessException(ErrorCode.OUT_OF_STOCK, "재고가 부족해요: " + product.getName());}
            order.addItem(OrderItem.create(product, cartItem.getQuantity()));
        }
        orderRepository.save(order);
        cartItemRepository.deleteAllByMemberId(memberId);
        return OrderDetailResponse.from(order);
    }

    public List<OrderSummaryResponse> getOrders(Long memberId) {return orderRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId).stream().map(OrderSummaryResponse::from).toList();
    }
    public OrderDetailResponse getOrder(Long memberId, String orderNumber) {return OrderDetailResponse.from(findMine(memberId, orderNumber));
    }

    @Transactional
    public OrderDetailResponse cancelOrder(Long memberId, String orderNumber) {
        Order order = findMine(memberId, orderNumber);
        order.cancel();
        paymentService.refund(order, "고객 요청 취소"); // 결제된 주문이면 환불 — 실패하면 취소도 롤백
        for (OrderItem item : order.getItems()) {productRepository.increaseStock(item.getProduct().getId(), item.getQuantity());
        }return OrderDetailResponse.from(order);
    }

    private Order findMine(Long memberId, String orderNumber) {
        return orderRepository.findByOrderNumberAndMemberId(orderNumber, memberId).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    private String generateOrderNumber() {
        String date = ORDER_DATE.format(Instant.now());
        for (int i = 0; i < 5; i++) {
            String candidate = date + "-" + String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
            if (!orderRepository.existsByOrderNumber(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("주문번호를 만들지 못했습니다.");
    }

}