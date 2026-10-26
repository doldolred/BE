package com.doldol.api.member;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    @EntityGraph(attributePaths = "member")
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /** 새 링크를 보내면 이전 링크는 못 쓰게 */
    @Modifying
    @Query("delete from PasswordResetToken t where t.member.id = :memberId and t.usedAt is null")
    int deleteUnused(@Param("memberId") Long memberId);

}
