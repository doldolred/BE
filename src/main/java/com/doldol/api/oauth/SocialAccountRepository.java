package com.doldol.api.oauth;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    @EntityGraph(attributePaths = "member")
    Optional<SocialAccount> findByProviderAndProviderUserId(SocialProvider provider, String providerUserId);

    boolean existsByProviderAndProviderUserId(SocialProvider provider, String providerUserId);

    /** 내 정보 화면 — 연결된 소셜 로그인 */
    @Query("select s.provider from SocialAccount s where s.member.id = :memberId order by s.id")
    List<SocialProvider> findProvidersByMemberId(@Param("memberId") Long memberId);

    void deleteAllByMemberId(Long memberId);

}