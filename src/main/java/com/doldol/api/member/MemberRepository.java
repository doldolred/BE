package com.doldol.api.member;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);

    /** 아이디(이메일) 찾기 — 이름 + 휴대폰 번호(숫자만 비교, 하이픈 유무 상관없이). 탈퇴 회원은 빼고 */
    @Query(value = """
            select * from member
            where name = :name and regexp_replace(coalesce(phone, ''), '[^0-9]', '', 'g') = :digits and withdrawn_at is null
            order by id
            """, nativeQuery = true)
    List<Member> findByNameAndPhoneDigits(@Param("name") String name, @Param("digits") String digits);

}