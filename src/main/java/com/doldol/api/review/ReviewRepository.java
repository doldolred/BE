package com.doldol.api.review;

import com.doldol.api.order.OrderItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // ── 손님 (상품 상세) — 숨긴 것 빼고 ──

    @EntityGraph(attributePaths = "member")
    @Query("""
            select r from Review r
            where r.product.id = :productId and r.hidden = false
              and (:photoOnly = false or size(r.imageUrls) > 0)""")
    Page<Review> findVisible(@Param("productId") Long productId, @Param("photoOnly") boolean photoOnly, Pageable pageable);

    @Query("""
            select new com.doldol.api.review.RatingCount(r.rating, count(r)) from Review r
            where r.product.id = :productId and r.hidden = false
            group by r.rating""")
    List<RatingCount> countByRating(@Param("productId") Long productId);

    // ── 회원 ──

    @EntityGraph(attributePaths = {"member", "product"})
    Page<Review> findByMemberId(Long memberId, Pageable pageable);

    Optional<Review> findByIdAndMemberId(Long id, Long memberId);

    boolean existsByOrderItemId(Long orderItemId);

    /** 후기를 쓸 수 있는 주문 상품 — 배송 완료된 내 주문 중 아직 후기가 없는 것, 최근 배송순 */
    @Query("""
            select oi from OrderItem oi join fetch oi.order o
            where o.member.id = :memberId and o.status = com.doldol.api.order.OrderStatus.DELIVERED
              and not exists (select r.id from Review r where r.orderItem = oi)
            order by o.deliveredAt desc, oi.id""")
    List<OrderItem> findWritableItems(@Param("memberId") Long memberId);

    // ── 관리자 ──

    @EntityGraph(attributePaths = {"member", "product"})
    @Query("""
            select r from Review r
            where (:hidden is null or r.hidden = :hidden)
              and (:best is null or r.best = :best)
              and (:noReply = false or r.reply is null)""")
    Page<Review> search(@Param("hidden") Boolean hidden, @Param("best") Boolean best, @Param("noReply") boolean noReply, Pageable pageable);

    @EntityGraph(attributePaths = {"member", "product"})
    Optional<Review> findWithMemberById(Long id);

}