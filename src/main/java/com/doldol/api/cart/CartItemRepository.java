package com.doldol.api.cart;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("select c from CartItem c join fetch c.product where c.member.id = :memberId order by c.id")
    List<CartItem> findAllWithProductByMemberId(@Param("memberId") Long memberId);
    Optional<CartItem> findByMemberIdAndProductId(Long memberId, Long productId);
    Optional<CartItem> findByIdAndMemberId(Long id, Long memberId);
    void deleteAllByMemberId(Long memberId);

}