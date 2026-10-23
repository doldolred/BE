package com.doldol.api.admin.order;
import com.doldol.api.admin.AdminPeriod;
import com.doldol.api.common.PageResponse;
import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.order.Order;
import com.doldol.api.order.OrderItem;
import com.doldol.api.order.OrderStatus;
import com.doldol.api.payment.PaymentService;
import com.doldol.api.product.ProductRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderService {
    private static final Instant MIN_TIME = Instant.EPOCH;
    private static final Instant MAX_TIME = Instant.parse("9999-12-31T00:00:00Z");
    private final AdminOrderRepository adminOrderRepository;
    private final ProductRepository productRepository;
    private final PaymentService paymentService;
    public PageResponse<AdminOrderSummaryResponse> search(OrderStatus status, LocalDate from, LocalDate to, String keyword, int page, int size){
        Set<OrderStatus>statuses = status == null ? EnumSet.allOf(OrderStatus.class) : EnumSet.of(status);
        Instant start = from == null ? MIN_TIME : from.atStartOfDay(AdminPeriod.KST).toInstant();
        Instant end = to == null ? MAX_TIME : to.plusDays(1).atStartOfDay(AdminPeriod.KST).toInstant();
        String kw = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        var pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.of(adminOrderRepository.search(statuses, start, end, kw, pageable), AdminOrderSummaryResponse::from);
    }
    public AdminOrderDetailResponse getOrder(String orderNumber){return AdminOrderDetailResponse.from(find(orderNumber));}
    @Transactional
    public AdminOrderDetailResponse changeStatus(String orderNumber, ChangeOrderStatusRequest request) {
        Order order = find(orderNumber);
        if (request.status() == OrderStatus.SHIPPING) {
            order.ship(request.courier(), request.trackingNumber());
        } else {
            order.changeStatus(request.status());
        }
        if (request.status() == OrderStatus.CANCELLED) {
            paymentService.refund(order, "관리자 취소"); // 결제된 주문이면 환불 — 실패하면 상태 변경도 롤백
            for (OrderItem item : order.getItems()) {
                productRepository.increaseStock(item.getProduct().getId(), item.getQuantity());
            }
        }
        return AdminOrderDetailResponse.from(order);
    }
    private Order find(String orderNumber){return adminOrderRepository.findByOrderNumber(orderNumber).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));}
}
