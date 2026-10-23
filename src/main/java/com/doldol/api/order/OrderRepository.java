package com.doldol.api.order;

import jakarta.persistence.LockModeType;
import java.time.Instant;
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

    @EntityGraph(attributePaths = "items")
    Optional<Order> findByOrderNumber(String orderNumber);

    /** 잠그고 읽기 (select ... for update) — 결제 반영 · 자동 취소가 같은 주문을 동시에 바꾸지 않게 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.orderNumber = :orderNumber")
    Optional<Order> findForUpdate(@Param("orderNumber") String orderNumber);

    /** 자동 취소 대상 — 이 상태로 before 보다 먼저 만들어진 주문 */
    @Query("select o.orderNumber from Order o where o.status = :status and o.createdAt < :before")
    List<String> findOrderNumbersByStatusBefore(@Param("status") OrderStatus status, @Param("before") Instant before);
}
