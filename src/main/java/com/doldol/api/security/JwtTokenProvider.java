package com.doldol.api.security;

import com.doldol.api.member.Member;
import com.doldol.api.member.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/** Access Token(JWT) 만들기 · 검증 */
@Component
public class JwtTokenProvider {

    private final SecretKey key;

    private final Duration validity;

    public JwtTokenProvider(JwtProperties properties) {
        // HS256 은 32바이트 이상 키가 필요 — 짧으면 여기서 바로 에러가 나서 서버가 안 뜬다
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.validity = properties.accessTokenValidity();
    }

    public String createAccessToken(Member member) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(member.getId()))
                .claim("email", member.getEmail())
                .claim("role", member.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(validity)))
                .signWith(key)
                .compact();
    }

    public long getValiditySeconds() {
        return validity.toSeconds();
    }

    /** 서명 · 만료를 확인하고 회원 정보를 꺼낸다. 잘못된 토큰이면 빈 값 */
    public Optional<LoginMember> parse(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return Optional.of(new LoginMember(Long.valueOf(claims.getSubject()), claims.get("email", String.class),
                    Role.valueOf(claims.get("role", String.class))));
        }
        catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

}