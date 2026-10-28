package com.doldol.api.review;

import com.doldol.api.common.BaseEntity;
import com.doldol.api.member.Member;
import com.doldol.api.order.OrderItem;
import com.doldol.api.product.Product;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 상품 후기 — 배송 완료된 주문 상품 하나에 하나. 관리자가 숨기거나 베스트로 고르고 답글을 단다 */
@Entity
@Table(name = "review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false, unique = true)
    private OrderItem orderItem;
    @Column(nullable = false, length = 50)
    private String optionName;
    @Column(nullable = false)
    private int rating;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @ElementCollection
    @CollectionTable(name = "review_image", joinColumns = @JoinColumn(name = "review_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "url", nullable = false, length = 300)
    private List<String> imageUrls = new ArrayList<>();
    @Column(nullable = false)
    private boolean hidden;
    @Column(nullable = false)
    private boolean best;
    @Column(columnDefinition = "TEXT")
    private String reply;
    private Instant repliedAt;
    public static Review create(Member member, OrderItem item, int rating, String content, List<String> imageUrls) {
        Review review = new Review();
        review.member = member;
        review.product = item.getProduct();
        review.orderItem = item;
        review.optionName = item.getOptionName();
        review.rating = rating;
        review.content = content.strip();
        review.imageUrls.addAll(imageUrls);
        return review;
    }
    public void changeHidden(boolean hidden) {this.hidden = hidden;}
    public void changeBest(boolean best) {this.best = best;}
    public void reply(String reply) {
        if (reply == null || reply.isBlank()) {
            this.reply = null;
            this.repliedAt = null;
        }
        else {
            this.reply = reply.strip();
            this.repliedAt = Instant.now();
        }
    }

}