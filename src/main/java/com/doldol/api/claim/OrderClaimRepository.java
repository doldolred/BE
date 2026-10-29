package com.doldol.api.claim;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderClaimRepository extends JpaRepository<OrderClaim, Long> {

    boolean existsByOrderIdAndStatusIn(Long orderId, Collection<ClaimStatus> statuses);

    // ── 회원 ──

    @EntityGraph(attributePaths = "order")
    Page<OrderClaim> findByMemberId(Long memberId, Pageable pageable);

    @EntityGraph(attributePaths = "order")
    Optional<OrderClaim> findByIdAndMemberId(Long id, Long memberId);

    @EntityGraph(attributePaths = "order")
    List<OrderClaim> findByOrderIdOrderByIdDesc(Long orderId);

    // ── 관리자 ──

    @EntityGraph(attributePaths = {"order", "member"})
    @Query("""
            select c from OrderClaim c
            where (:status is null or c.status = :status)
              and (:type is null or c.type = :type)""")
    Page<OrderClaim> search(@Param("status") ClaimStatus status, @Param("type") ClaimType type, Pageable pageable);

    @EntityGraph(attributePaths = {"order", "member"})
    Optional<OrderClaim> findWithOrderById(Long id);

    long countByStatus(ClaimStatus status);

}