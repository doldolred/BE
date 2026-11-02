package com.doldol.api.point;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    /** 지금 쓸 수 있는 잔액 */
    @Query("select coalesce(sum(p.remaining), 0) from PointHistory p where p.member.id = :memberId and p.remaining > 0 and p.expiresAt > :now")
    long sumUsable(@Param("memberId") Long memberId, @Param("now") Instant now);

    /** now ~ until 사이에 소멸할 금액 */
    @Query("""
            select coalesce(sum(p.remaining), 0) from PointHistory p
            where p.member.id = :memberId and p.remaining > 0 and p.expiresAt > :now and p.expiresAt <= :until
            """)
    long sumExpiring(@Param("memberId") Long memberId, @Param("now") Instant now, @Param("until") Instant until);

    /** 쓸 수 있는 적립 줄을 잠그고 읽기 — 먼저 소멸할 것부터. 같은 회원이 동시에 두 번 써도 잔액을 넘지 않게 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PointHistory p where p.member.id = :memberId and p.remaining > 0 and p.expiresAt > :now order by p.expiresAt, p.id")
    List<PointHistory> findUsableForUpdate(@Param("memberId") Long memberId, @Param("now") Instant now);

    /** 유효기간이 지났는데 남은 금액이 있는 적립 줄 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PointHistory p where p.remaining > 0 and p.expiresAt <= :now")
    List<PointHistory> findExpired(@Param("now") Instant now);

    @EntityGraph(attributePaths = "order")
    Page<PointHistory> findByMemberId(Long memberId, Pageable pageable);

    boolean existsByOrderIdAndType(Long orderId, PointType type);

}