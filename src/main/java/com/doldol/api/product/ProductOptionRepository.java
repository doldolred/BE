package com.doldol.api.product;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    @EntityGraph(attributePaths = "product")
    Optional<ProductOption> findWithProductById(Long id);

    /** 재고가 충분할 때만 줄인다 — 0 이 돌아오면 재고 부족 (동시에 주문해도 음수가 되지 않는다) */
    @Modifying
    @Query("update ProductOption o set o.stock = o.stock - :quantity where o.id = :id and o.stock >= :quantity")
    int decreaseStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying
    @Query("update ProductOption o set o.stock = o.stock + :quantity where o.id = :id")
    int increaseStock(@Param("id") Long id, @Param("quantity") int quantity);

}
