package com.doldol.api.member;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.security.JwtProperties;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 리프레시 토큰 발급 · 교체 · 폐기. 한 번 쓴 토큰은 지우고 새 토큰을 준다 (훔친 토큰을 오래 못 쓰게) */
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtProperties properties;

    public record Rotated(Member member, String token) {
    }

    /** 새 토큰 원문을 돌려준다 (쿠키에 넣을 값). 이 회원의 만료된 토큰은 겸사겸사 정리 */
    public String issue(Member member) {
        Instant now = Instant.now();
        refreshTokenRepository.deleteExpired(member.getId(), now);
        String token = Tokens.newToken();
        refreshTokenRepository.save(RefreshToken.create(member, Tokens.hash(token), now.plus(properties.refreshTokenValidity())));
        return token;
    }

    /** 쓰고 버린다 → 새 토큰. 없거나 만료면 401 */
    public Rotated rotate(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        RefreshToken saved = refreshTokenRepository.findByTokenHash(Tokens.hash(token))
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));
        refreshTokenRepository.delete(saved);
        if (saved.isExpired()) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        Member member = saved.getMember();
        return new Rotated(member, issue(member));
    }

    /** 로그아웃 — 이 기기의 토큰만 */
    public void revoke(String token) {
        if (token != null && !token.isBlank()) {
            refreshTokenRepository.findByTokenHash(Tokens.hash(token)).ifPresent(refreshTokenRepository::delete);
        }
    }

    /** 비밀번호를 바꾸면 모든 기기에서 로그아웃 */
    public void revokeAll(Long memberId) {
        refreshTokenRepository.deleteAllByMemberId(memberId);
    }

}
