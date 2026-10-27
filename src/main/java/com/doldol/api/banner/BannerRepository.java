package com.doldol.api.banner;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BannerRepository extends JpaRepository<Banner, Long> {

    /** 지금 보여 줄 배너 — 켜져 있고 기간 안(비어 있으면 제한 없음), 정한 순서대로 */
    @Query("""
            select b from Banner b
            where b.active = true
              and (b.startsAt is null or b.startsAt <= :now)
              and (b.endsAt is null or b.endsAt > :now)
            order by b.sortOrder, b.id""")
    List<Banner> findShowing(@Param("now") Instant now);

    List<Banner> findAllByOrderBySortOrderAscIdAsc();

}