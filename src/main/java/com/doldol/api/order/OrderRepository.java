package com.doldol.api.order;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = "items")
    List<Order> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);
    @EntityGraph(attributePaths = "items")
    Optional<Order> findByOrderNumberAndMemberId(String orderNumber, Long memberId);
    boolean existsByOrderNumber(String orderNumber);
}
