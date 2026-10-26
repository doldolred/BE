package com.doldol.api.shipping;

import com.doldol.api.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 추가 배송비 지역 — 우편번호 구간 (예: 제주 63000~63644, +3,000원). 관리자가 관리 */
@Entity
@Table(name = "shipping_area")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShippingArea extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 5)
    private String zipFrom;

    @Column(nullable = false, length = 5)
    private String zipTo;

    @Column(nullable = false)
    private int extraFee;

    public static ShippingArea create(String name, String zipFrom, String zipTo, int extraFee) {
        ShippingArea area = new ShippingArea();
        area.name = name.trim();
        area.zipFrom = zipFrom;
        area.zipTo = zipTo;
        area.extraFee = extraFee;
        return area;
    }

}
