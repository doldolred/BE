package com.doldol.api.order;

import com.doldol.api.claim.ClaimStatus;
import com.doldol.api.claim.ClaimType;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = "items")
    List<Order> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);
    @EntityGraph(attributePaths = "items")
    Optional<Order> findByOrderNumberAndMemberId(String orderNumber, Long memberId);
    boolean existsByOrderNumber(String orderNumber);
    /** 탈퇴 전에 진행 중인 주문이 있는지 */
    boolean existsByMemberIdAndStatusIn(Long memberId, Collection<OrderStatus> statuses);

    @EntityGraph(attributePaths = "items")
    Optional<Order> findByOrderNumber(String orderNumber);

    /** 잠그고 읽기 (select ... for update) — 결제 반영 · 자동 취소가 같은 주문을 동시에 바꾸지 않게 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.orderNumber = :orderNumber")
    Optional<Order> findForUpdate(@Param("orderNumber") String orderNumber);

    /** 자동 취소 대상 — 이 상태로 before 보다 먼저 만들어진 주문 */
    @Query("select o.orderNumber from Order o where o.status = :status and o.createdAt < :before")
    List<String> findOrderNumbersByStatusBefore(@Param("status") OrderStatus status, @Param("before") Instant before);

    /**
     * 자동 구매확정 대상 (V15) — 배송 완료 후 before 가 지났고, 확정 전이며, 진행 중인 신청이 없는 주문.
     * 교환품을 보낸 주문은 보낸 때(교환 완료)부터 다시 기간을 센다 — 교환품을 받기 전에 확정되지 않게
     */
    @Query("""
            select o.orderNumber from Order o
            where o.status = :status and o.confirmedAt is null and o.deliveredAt < :before
              and not exists (select c.id from OrderClaim c where c.order = o
                              and (c.status in :open or (c.type = :exchange and c.completedAt >= :before)))
            """)
    List<String> findConfirmTargets(@Param("status") OrderStatus status, @Param("before") Instant before,
                                    @Param("open") Collection<ClaimStatus> open, @Param("exchange") ClaimType exchange);
}
