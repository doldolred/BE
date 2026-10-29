package com.doldol.api.claim;

import com.doldol.api.common.BaseEntity;
import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.member.Member;
import com.doldol.api.order.Order;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_claim")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderClaim extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClaimType type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ClaimReason reason;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String detail;
    @ElementCollection
    @CollectionTable(name = "order_claim_image", joinColumns = @JoinColumn(name = "claim_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "url", nullable = false, length = 300)
    private List<String> imageUrls = new ArrayList<>();
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClaimStatus status;
    @Column(nullable = false)
    private int returnShippingFee;
    @Column(nullable = false)
    private int refundAmount;
    @Column(length = 500)
    private String rejectReason;
    @Column(length = 30)
    private String exchangeCourier;
    @Column(length = 50)
    private String exchangeTrackingNumber;
    private Instant approvedAt;
    private Instant completedAt;
    public static OrderClaim create(Order order, Member member, ClaimType type, ClaimReason reason, String detail, List<String> imageUrls, int returnShippingFee, int refundAmount) {
        OrderClaim claim = new OrderClaim();
        claim.order = order;
        claim.member = member;
        claim.type = type;
        claim.reason = reason;
        claim.detail = detail.strip();
        claim.imageUrls.addAll(imageUrls);
        claim.status = ClaimStatus.REQUESTED;
        claim.returnShippingFee = returnShippingFee;
        claim.refundAmount = refundAmount;
        return claim;
    }

    public void withdraw() {
        require(ClaimStatus.REQUESTED);
        status = ClaimStatus.WITHDRAWN;
    }

    public void approve() {
        require(ClaimStatus.REQUESTED);
        status = ClaimStatus.APPROVED;
        approvedAt = Instant.now();
    }

    public void reject(String rejectReason) {
        if (!ClaimStatus.OPEN.contains(status)) {
            throw new BusinessException(ErrorCode.INVALID_CLAIM_STATUS);
        }
        status = ClaimStatus.REJECTED;
        this.rejectReason = rejectReason.strip();
    }

    public void complete() {
        require(type == ClaimType.CANCEL ? ClaimStatus.REQUESTED : ClaimStatus.APPROVED);
        status = ClaimStatus.COMPLETED;
        Instant now = Instant.now();
        if (approvedAt == null) {
            approvedAt = now;
        }
        completedAt = now;
    }
    public void shipExchange(String courier, String trackingNumber) {
        this.exchangeCourier = courier.trim();
        this.exchangeTrackingNumber = trackingNumber.trim();
    }

    private void require(ClaimStatus expected) {if (status != expected) {throw new BusinessException(ErrorCode.INVALID_CLAIM_STATUS);
        }
    }

}