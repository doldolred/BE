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
    public static Member create(String email, String encodedPassword, String name, String phone){
        Member member = new Member();
        member.email = email;
        member.password = encodedPassword;
        member.name = name;
        member.phone = phone;
        member.role = Role.USER;
        return member;
    }
//관리자 권한 변경
    public void changeRole(Role role){this.role = role;}
}
