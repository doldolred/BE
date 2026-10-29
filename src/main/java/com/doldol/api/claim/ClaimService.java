package com.doldol.api.claim;

import com.doldol.api.admin.claim.ClaimCompleteRequest;
import com.doldol.api.common.PageResponse;
import com.doldol.api.coupon.CouponService;
import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.mail.MailService;
import com.doldol.api.member.Member;
import com.doldol.api.order.Order;
import com.doldol.api.order.OrderItem;
import com.doldol.api.order.OrderRepository;
import com.doldol.api.order.OrderStatus;
import com.doldol.api.payment.PaymentService;
import com.doldol.api.product.ProductOptionRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClaimService {

    private static final Sort LATEST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    private final OrderClaimRepository claimRepository;
    private final OrderRepository orderRepository;
    private final ProductOptionRepository productOptionRepository;
    private final PaymentService paymentService;
    private final CouponService couponService;
    private final MailService mailService;
    private final Duration returnPeriod;
    private final int returnShippingFee;
    private final String frontendUrl;
    public ClaimService(OrderClaimRepository claimRepository, OrderRepository orderRepository, ProductOptionRepository productOptionRepository,
                        PaymentService paymentService, CouponService couponService, MailService mailService,
                        @Value("${app.claim.return-days:7}") int returnDays, @Value("${app.claim.return-shipping-fee:6000}") int returnShippingFee,
                        @Value("${app.frontend-url}") String frontendUrl) {
        this.claimRepository = claimRepository;
        this.orderRepository = orderRepository;
        this.productOptionRepository = productOptionRepository;
        this.paymentService = paymentService;
        this.couponService = couponService;
        this.mailService = mailService;
        this.returnPeriod = Duration.ofDays(returnDays);
        this.returnShippingFee = returnShippingFee;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
    }

    // ── 회원 ─────────────────────────────

    public ClaimResponse create(Long memberId, String orderNumber, CreateClaimRequest r) {
        Order order = orderRepository.findByOrderNumberAndMemberId(orderNumber, memberId).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (claimRepository.existsByOrderIdAndStatusIn(order.getId(), ClaimStatus.OPEN)) {
            throw new BusinessException(ErrorCode.CLAIM_IN_PROGRESS);
        }
        switch (r.type()) {
            case CANCEL -> {
                if (order.getStatus() != OrderStatus.PREPARING) {
                    throw notAllowed(order.getStatus().isCancellable()
                            ? "아직 상품 준비 전이라 주문 상세에서 바로 취소할 수 있어요."
                            : "상품 준비 중인 주문만 취소 요청할 수 있어요.");
                }
            }
            case RETURN, EXCHANGE -> {
                if (order.getStatus() != OrderStatus.DELIVERED || order.getDeliveredAt() == null
                        || Instant.now().isAfter(order.getDeliveredAt().plus(returnPeriod))) {
                    throw notAllowed("배송 완료 후 %d일 안에만 신청할 수 있어요.".formatted(returnPeriod.toDays()));
                }
                if (r.type() == ClaimType.EXCHANGE && !r.reason().isSellerFault()) {
                    throw notAllowed("교환은 상품 불량 · 오배송일 때만 할 수 있어요.");
                }
            }
        }
        int fee = r.type() == ClaimType.RETURN && !r.reason().isSellerFault() ? returnShippingFee : 0;
        int refund = switch (r.type()) {
            case CANCEL -> order.getTotalPrice();
            case RETURN -> Math.max(0, order.getTotalPrice() - fee);
            case EXCHANGE -> 0;
        };
        List<String> images = r.imageUrls() == null ? List.of() : r.imageUrls();
        OrderClaim claim = OrderClaim.create(order, order.getMember(), r.type(), r.reason(), r.detail(), images, fee, refund);
        return ClaimResponse.forMember(claimRepository.saveAndFlush(claim));
    }

    @Transactional(readOnly = true)
    public PageResponse<ClaimResponse> getMine(Long memberId, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50), LATEST);
        return PageResponse.of(claimRepository.findByMemberId(memberId, pageable), ClaimResponse::forMember);
    }

    @Transactional(readOnly = true)
    public ClaimResponse getMine(Long memberId, Long id) {
        return ClaimResponse.forMember(findMine(memberId, id));
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> getByOrder(Long memberId, String orderNumber) {
        Order order = orderRepository.findByOrderNumberAndMemberId(orderNumber, memberId).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        return claimRepository.findByOrderIdOrderByIdDesc(order.getId()).stream().map(ClaimResponse::forMember).toList();
    }

    public ClaimResponse withdraw(Long memberId, Long id) {
        OrderClaim claim = findMine(memberId, id);
        claim.withdraw();
        return ClaimResponse.forMember(claim);
    }
    private OrderClaim findMine(Long memberId, Long id) {return claimRepository.findByIdAndMemberId(id, memberId).orElseThrow(() -> new BusinessException(ErrorCode.CLAIM_NOT_FOUND));}

    // ── 관리자 ───────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<ClaimResponse> search(ClaimStatus status, ClaimType type, int page, int size) {PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 100), LATEST);return PageResponse.of(claimRepository.search(status, type, pageable), ClaimResponse::forAdmin);}
    @Transactional(readOnly = true)
    public ClaimResponse get(Long id) {return ClaimResponse.forAdmin(find(id));}
    @Transactional(readOnly = true)
    public long countRequested() {return claimRepository.countByStatus(ClaimStatus.REQUESTED);}

    public ClaimResponse approve(Long id) {
        OrderClaim claim = find(id);
        Order order = claim.getOrder();
        if (claim.getType() == ClaimType.CANCEL) {
            claim.complete();
            order.changeStatus(OrderStatus.CANCELLED); // 그새 배송중이 됐으면 여기서 막힌다
            paymentService.refund(order, "취소 요청 승인");
            for (OrderItem item : order.getItems()) {
                productOptionRepository.increaseStock(item.getOption().getId(), item.getQuantity());
            }
            couponService.release(order);
            notify(claim, "취소가 완료됐어요", "결제하신 금액은 카드사에 따라 3~7일 안에 환불돼요.");
        }
        else {
            claim.approve();
            notify(claim, claim.getType() == ClaimType.RETURN ? "반품 신청이 승인됐어요" : "교환 신청이 승인됐어요",
                    "택배 기사님이 상품을 회수하러 찾아갈 거예요. 받은 상자에 담아 두시면 돼요.");
        }
        return ClaimResponse.forAdmin(claim);
    }

    public ClaimResponse reject(Long id, String reason) {
        OrderClaim claim = find(id);
        claim.reject(reason);
        notify(claim, "신청을 처리하지 못했어요", "사유: " + claim.getRejectReason());
        return ClaimResponse.forAdmin(claim);
    }

    public ClaimResponse complete(Long id, ClaimCompleteRequest r) {
        OrderClaim claim = find(id);
        Order order = claim.getOrder();
        switch (claim.getType()) {
            case CANCEL -> throw new BusinessException(ErrorCode.INVALID_CLAIM_STATUS, "취소 요청은 승인하면 바로 끝나요.");
            case RETURN -> {
                claim.complete();
                paymentService.refund(order, "반품 완료", claim.getRefundAmount());
                order.markReturned();
                couponService.release(order);
                notify(claim, "반품이 완료됐어요", "%,d원이 카드사에 따라 3~7일 안에 환불돼요.".formatted(claim.getRefundAmount())
                        + (claim.getReturnShippingFee() > 0 ? " (반품 배송비 %,d원 제외)".formatted(claim.getReturnShippingFee()) : ""));
            }
            case EXCHANGE -> {
                if (r == null || r.courier() == null || r.courier().isBlank() || r.trackingNumber() == null || r.trackingNumber().isBlank()) {
                    throw new BusinessException(ErrorCode.INVALID_INPUT, "교환 상품을 보낸 택배사와 송장번호를 입력해 주세요.");
                }
                claim.complete();
                claim.shipExchange(r.courier(), r.trackingNumber());
                notify(claim, "교환 상품을 보냈어요", "%s %s 로 보냈어요.".formatted(claim.getExchangeCourier(), claim.getExchangeTrackingNumber()));
            }
        }
        return ClaimResponse.forAdmin(claim);
    }

    private OrderClaim find(Long id) {
        return claimRepository.findWithOrderById(id).orElseThrow(() -> new BusinessException(ErrorCode.CLAIM_NOT_FOUND));
    }

    private static BusinessException notAllowed(String message) {
        return new BusinessException(ErrorCode.CLAIM_NOT_ALLOWED, message);
    }

    private void notify(OrderClaim claim, String title, String body) {
        Member member = claim.getMember();
        String orderNumber = claim.getOrder().getOrderNumber();
        mailService.send(member.getEmail(), "[돌돌] " + title, """
                %s님, 안녕하세요. 돌돌입니다.

                주문 %s 의 %s
                %s

                자세한 내용은 주문 상세에서 볼 수 있어요.
                %s/mypage/orders/%s
                """.formatted(member.getName(), orderNumber, title, body, frontendUrl, orderNumber));
    }

}