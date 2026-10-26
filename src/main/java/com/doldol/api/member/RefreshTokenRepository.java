package com.doldol.api.member;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @EntityGraph(attributePaths = "member")
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("delete from RefreshToken t where t.member.id = :memberId")
    int deleteAllByMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query("delete from RefreshToken t where t.member.id = :memberId and t.expiresAt < :now")
    int deleteExpired(@Param("memberId") Long memberId, @Param("now") Instant now);

}
