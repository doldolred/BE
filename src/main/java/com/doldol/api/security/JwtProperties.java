package com.doldol.api.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * app.jwt.* — accessTokenValidity: 짧게(기본 30분) · refreshTokenValidity: 로그인 유지 기간(기본 14일)
 * cookieSecure: 리프레시 쿠키를 https 에서만 보낼지 (운영 true, 로컬 http 개발 false)
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration accessTokenValidity, Duration refreshTokenValidity, boolean cookieSecure) {

}
