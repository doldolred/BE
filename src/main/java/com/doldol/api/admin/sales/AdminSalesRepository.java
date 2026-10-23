package com.doldol.api.admin.sales;

import com.doldol.api.order.Order;
import com.doldol.api.order.OrderStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** 매출 집계 전용 — 조회만 하므로 JpaRepository 대신 빈 Repository 를 확장한다 */
public interface AdminSalesRepository extends Repository<Order, Long> {

    /** 기간 안, 주어진 상태들의 금액 합계 · 주문 수 */
    @Query("""
			select new com.doldol.api.admin.sales.SalesTotal(coalesce(sum(o.totalPrice), 0L), count(o))
			from Order o
			where o.status in :statuses and o.createdAt >= :from and o.createdAt < :to
			""")
    SalesTotal total(@Param("statuses") Collection<OrderStatus> statuses, @Param("from") Instant from,
                     @Param("to") Instant to);

    /**
     * 일별 매출 — 한국 날짜로 묶어야 해서 PostgreSQL 네이티브 쿼리 (DB 시각은 UTC).
     * 상태는 문자열로 넘긴다 (예: "PAID").
     */
    @Query(value = """
			select to_char(o.created_at at time zone 'Asia/Seoul', 'YYYY-MM-DD') as day,
			       coalesce(sum(o.total_price), 0) as sales,
			       count(*) as orders
			from orders o
			where o.status in (:statuses) and o.created_at >= :from and o.created_at < :to
			group by 1
			order by 1
			""", nativeQuery = true)
    List<DailySalesRow> daily(@Param("statuses") Collection<String> statuses, @Param("from") Instant from,
                              @Param("to") Instant to);

    /** 많이 팔린 상품 — 금액순. 개수는 Pageable 로 자른다 */
    @Query("""
			select new com.doldol.api.admin.sales.TopProductRow(
			    i.product.id, i.productName, sum(i.quantity), sum(i.unitPrice * i.quantity))
			from OrderItem i join i.order o
			where o.status in :statuses and o.createdAt >= :from and o.createdAt < :to
			group by i.product.id, i.productName
			order by sum(i.unitPrice * i.quantity) desc
			""")
    List<TopProductRow> topProducts(@Param("statuses") Collection<OrderStatus> statuses, @Param("from") Instant from,
                                    @Param("to") Instant to, Pageable pageable);

}