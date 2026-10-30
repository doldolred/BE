package com.doldol.api.member;
import com.doldol.api.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false, length = 50)
    private String name;
    @Column(length = 20)
    private String phone;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length =20)
    private Role role;
    /** 가입 동의 (V6) — 이용약관 · 개인정보 수집 · 이용은 필수, 마케팅 수신은 선택 */
    private Instant termsAgreedAt;
    private Instant privacyAgreedAt;
    @Column(nullable = false)
    private boolean marketingAgreed;
    /** 마케팅 수신에 마지막으로 동의 / 철회한 시각 */
    private Instant marketingAgreedAt;
    /** 탈퇴 시각 (V14) — 탈퇴하면 이메일 · 이름 · 휴대폰을 지우고 로그인할 수 없게 한다 */
    private Instant withdrawnAt;
    public static Member create(String email, String encodedPassword, String name, String phone, boolean marketingAgreed){
        Member member = new Member();
        Instant now = Instant.now();
        member.email = email;
        member.password = encodedPassword;
        member.name = name;
        member.phone = phone;
        member.role = Role.USER;
        member.termsAgreedAt = now;
        member.privacyAgreedAt = now;
        member.marketingAgreed = marketingAgreed;
        member.marketingAgreedAt = now;
        return member;
    }
//관리자 권한 변경
    public void changeRole(Role role){this.role = role;}
    public void changePassword(String encodedPassword){this.password = encodedPassword;}
    /** 탈퇴 — 주문 기록과의 연결만 남기고 개인정보를 지운다. 이메일은 유일해야 해서 id 로 채운다 */
    public void withdraw(String encodedRandomPassword){
        this.email = "withdrawn-" + id + "@doldol.invalid";
        this.password = encodedRandomPassword;
        this.name = "탈퇴회원";
        this.phone = null;
        this.marketingAgreed = false;
        this.marketingAgreedAt = Instant.now();
        this.withdrawnAt = Instant.now();
    }
    public void changeMarketingAgreed(boolean agreed){
        this.marketingAgreed = agreed;
        this.marketingAgreedAt = Instant.now();
    }
}
