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
import java.util.HashMap;
import java.util.Map;
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

    /**
     * 잠깐 쓰고 버리는 서명 토큰 (V17 소셜 첫 로그인 → 약관 동의 화면까지 가입 정보를 들고 다닌다).
     * purpose 가 다르면 못 쓰고, 로그인 토큰으로도 못 쓴다 (subject 가 없음)
     */
    public String createPurposeToken(String purpose, Map<String, String> claims, Duration ttl) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(claims)
                .claim("purpose", purpose)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }

    /** 서명 · 만료 · purpose 를 확인하고 문자열 값만 꺼낸다. 잘못된 토큰이면 빈 값 */
    public Optional<Map<String, String>> parsePurposeToken(String purpose, String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            if (!purpose.equals(claims.get("purpose", String.class))) {
                return Optional.empty();
            }
            Map<String, String> values = new HashMap<>();
            claims.forEach((k, v) -> {
                if (v instanceof String s) {
                    values.put(k, s);
                }
            });
            return Optional.of(values);
        }
        catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
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