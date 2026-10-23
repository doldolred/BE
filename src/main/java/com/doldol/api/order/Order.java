package com.doldol.api.order;

import com.doldol.api.common.BaseEntity;
import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.member.Member;
import com.doldol.api.payment.Payment;
import com.doldol.api.payment.PaymentStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 주문. order 는 SQL 예약어라 테이블 이름은 orders */
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 화면 · URL 에 보이는 번호 (예: 20261019-482913). id 를 밖에 드러내지 않기 위해 따로 둔다 */
    @Column(nullable = false, unique = true, length = 30)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false)
    private int totalPrice;

    @Column(nullable = false, length = 50)
    private String receiverName;

    @Column(nullable = false, length = 20)
    private String receiverPhone;

    @Column(nullable = false, length = 10)
    private String zipcode;

    @Column(nullable = false)
    private String address1;

    private String address2;

    private String memo;

    /** 배송 정보 (V4) — 관리자가 배송중으로 바꿀 때 채운다 */
    @Column(length = 30)
    private String courier;

    @Column(length = 50)
    private String trackingNumber;

    private Instant shippedAt;

    private Instant deliveredAt;

    /** 주문을 저장하면 주문 상품도 같이 저장된다 (cascade) */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    /** 결제 기록 (최신순). 저장은 PaymentRepository 가 한다 — 여기선 읽기만 */
    @OneToMany(mappedBy = "order")
    @OrderBy("id desc")
    private List<Payment> payments = new ArrayList<>();

    public static Order create(String orderNumber, Member member, CreateOrderRequest request) {
        Order order = new Order();
        order.orderNumber = orderNumber;
        order.member = member;
        order.status = OrderStatus.PENDING_PAYMENT;
        order.receiverName = request.receiverName().trim();
        order.receiverPhone = request.receiverPhone().trim();
        order.zipcode = request.zipcode().trim();
        order.address1 = request.address1().trim();
        order.address2 = blankToNull(request.address2());
        order.memo = blankToNull(request.memo());
        return order;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.assignOrder(this);
        totalPrice += item.getLineTotal();
    }

    /** 결제 승인 반영 — 결제대기 → 결제완료 */
    public void pay(Payment payment) {
        moveTo(OrderStatus.PAID);
        payments.addFirst(payment);
    }

    /** 이 결제(나이스페이 tid)로 이미 결제됐는가 — 같은 결과가 두 번 올 때 */
    public boolean isPaidWith(String paymentKey) {
        return payments.stream().anyMatch(p -> p.getStatus() != PaymentStatus.FAILED && p.getPaymentKey().equals(paymentKey));
    }

    /** 고객 취소 — 결제대기 · 결제완료일 때만 */
    public void cancel() {
        if (!status.isCancellable()) {
            throw new BusinessException(ErrorCode.ORDER_NOT_CANCELLABLE);
        }
        status = OrderStatus.CANCELLED;
    }

    /** 관리자 상태 변경 — OrderStatus.canMoveTo 규칙을 따른다. 배송중은 ship() 으로 */
    public void changeStatus(OrderStatus next) {
        if (next == OrderStatus.SHIPPING) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "배송중으로 바꿀 때는 택배사와 송장번호가 필요합니다.");
        }
        moveTo(next);
        if (next == OrderStatus.DELIVERED) {
            deliveredAt = Instant.now();
        }
    }

    /** 관리자 발송 처리 — 준비중 → 배송중 + 송장 */
    public void ship(String courier, String trackingNumber) {
        if (courier == null || courier.isBlank() || trackingNumber == null || trackingNumber.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "택배사와 송장번호를 입력해 주세요.");
        }
        moveTo(OrderStatus.SHIPPING);
        this.courier = courier.trim();
        this.trackingNumber = trackingNumber.trim();
        this.shippedAt = Instant.now();
    }

    private void moveTo(OrderStatus next) {
        if (!status.canMoveTo(next)) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "'" + status + "' 에서 '" + next + "' (으)로 바꿀 수 없습니다.");
        }
        status = next;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

}