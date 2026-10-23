package com.doldol.api.admin.order;

import com.doldol.api.order.Order;
import com.doldol.api.order.OrderStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 관리자용 주문 조회. 고객용 OrderRepository 와 같은 엔티티를 보지만 쿼리가 달라서 따로 둔다.
 */
public interface AdminOrderRepository extends JpaRepository<Order, Long> {

    /**
     * 조건 검색. 조건이 비었을 때 null 을 넘기지 않는다 — PostgreSQL 이 null 파라미터의 타입을 몰라
     * "varchar ~~ bytea" 오류가 난다. 대신 서비스가 "전부 걸리는 값"(모든 상태 · 아주 넓은 기간 · 빈 검색어)을 넘긴다.
     */
    @EntityGraph(attributePaths = "member")
    @Query("""
			select o from Order o join o.member m
			where o.status in :statuses
			  and o.createdAt >= :from and o.createdAt < :to
			  and (o.orderNumber like concat('%', :keyword, '%')
			       or lower(o.receiverName) like concat('%', :keyword, '%')
			       or lower(m.email) like concat('%', :keyword, '%')
			       or lower(m.name) like concat('%', :keyword, '%'))
			""")
    Page<Order> search(@Param("statuses") Collection<OrderStatus> statuses, @Param("from") Instant from,
                       @Param("to") Instant to, @Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = { "member", "items" })
    Optional<Order> findByOrderNumber(String orderNumber);

    /** 회원 상세 — 최근 주문 5건 */
    @EntityGraph(attributePaths = "items")
    List<Order> findTop5ByMemberIdOrderByCreatedAtDesc(Long memberId);

    /** 회원 목록 — 회원별 주문 수 · 총 구매액(매출로 잡는 상태만) 을 한 번에 */
    @Query("""
			select new com.doldol.api.admin.order.MemberOrderStat(
			    o.member.id,
			    count(o),
			    coalesce(sum(case when o.status in :sales then o.totalPrice else 0 end), 0L))
			from Order o
			where o.member.id in :memberIds
			group by o.member.id
			""")
    List<MemberOrderStat> statsByMembers(@Param("memberIds") Collection<Long> memberIds,
                                         @Param("sales") Collection<OrderStatus> sales);

}