package com.doldol.api.point;

import com.doldol.api.member.Member;
import com.doldol.api.order.Order;
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
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 적립금 증감 한 줄. 적립 줄(+)은 remaining 으로 남은 금액을 들고, 쓸 때 유효기간이 먼저 끝나는 줄부터 깎는다 */
@Entity
@Table(name = "point_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PointType type;
    @Column(nullable = false)
    private int amount;
    private Integer remaining;
    private Instant expiresAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    @Column(length = 100)
    private String memo;
    @Column(nullable = false)
    private Instant createdAt;

    public static PointHistory credit(Member member, PointType type, int amount, Order order, String memo, Instant expiresAt) {
        PointHistory h = base(member, type, amount, order, memo);
        h.remaining = amount;
        h.expiresAt = expiresAt;
        return h;
    }
    public static PointHistory debit(Member member, PointType type, int amount, Order order, String memo) {return base(member, type, -amount, order, memo);}
    private static PointHistory base(Member member, PointType type, int amount, Order order, String memo) {
        PointHistory h = new PointHistory();
        h.member = member;
        h.type = type;
        h.amount = amount;
        h.order = order;
        h.memo = memo == null || memo.isBlank() ? null : memo.strip();
        h.createdAt = Instant.now();
        return h;
    }
    void take(int n) {
        remaining -= n;
    }

}