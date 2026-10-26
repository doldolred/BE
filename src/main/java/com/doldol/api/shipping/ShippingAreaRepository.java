package com.doldol.api.shipping;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShippingAreaRepository extends JpaRepository<ShippingArea, Long> {

    /** 우편번호가 걸리는 구간 — 겹치면 추가 배송비가 큰 것부터. 우편번호는 5자리 숫자라 문자열 비교로 충분하다 */
    @Query("select a from ShippingArea a where :zipcode between a.zipFrom and a.zipTo order by a.extraFee desc")
    List<ShippingArea> findMatching(@Param("zipcode") String zipcode);

    List<ShippingArea> findAllByOrderByZipFromAsc();

}
