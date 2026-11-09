package com.doldol.api.address;

import com.doldol.api.common.BaseEntity;
import com.doldol.api.member.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 저장한 배송지 (V16) — 회원마다 최대 10개, 그중 하나가 기본 배송지 */
@Entity
@Table(name = "member_address")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAddress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    @Column(nullable = false, length = 20)
    private String label;
    @Column(nullable = false, length = 50)
    private String receiverName;
    @Column(nullable = false, length = 20)
    private String receiverPhone;
    @Column(nullable = false, length = 10)
    private String zipcode;
    @Column(nullable = false)
    private String address1;
    private String address2;
    @Column(name = "is_default", nullable = false)
    private boolean defaultAddress;

    public static MemberAddress create(Member member, AddressRequest request) {
        MemberAddress address = new MemberAddress();
        address.member = member;
        address.update(request);
        return address;
    }

    public void update(AddressRequest request) {
        this.label = request.label().strip();
        this.receiverName = request.receiverName().strip();
        this.receiverPhone = request.receiverPhone().strip();
        this.zipcode = request.zipcode();
        this.address1 = request.address1().strip();
        this.address2 = request.address2() == null || request.address2().isBlank() ? null : request.address2().strip();
    }

    public void markDefault(boolean defaultAddress) {
        this.defaultAddress = defaultAddress;
    }

}