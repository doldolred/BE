package com.doldol.api.order;

import com.doldol.api.common.BaseEntity;
import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.member.Member;
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
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
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
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
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
    public void addItem(OrderItem item){
        items.add(item);
        item.assignOrder(this);
        totalPrice += item.getLineTotal();
    }
    public void cancel(){
        if(!status.isCancellable()){throw new BusinessException(ErrorCode.ORDER_NOT_CANCELLABLE);
    }status = OrderStatus.CANCELLED;}
    private static String blankToNull(String value){
        return value == null || value.isBlank() ? null : value.trim();
    }
}
