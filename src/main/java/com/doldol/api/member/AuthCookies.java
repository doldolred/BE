package com.doldol.api.member;

import com.doldol.api.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * 리프레시 토큰 쿠키. HttpOnly(자바스크립트가 못 읽음) · SameSite=Lax(다른 사이트에서 보낸 POST 에는 안 실림)
 * · /api/auth 경로에만 실린다 — 다른 API 요청에는 붙지 않는다.
 */
@Component
@RequiredArgsConstructor
public class AuthCookies {

    public static final String NAME = "doldol_rt";

    private static final String PATH = "/api/auth";

    private final JwtProperties properties;

    public ResponseCookie issue(String refreshToken) {
        return base(refreshToken).maxAge(properties.refreshTokenValidity()).build();
    }

    public ResponseCookie clear() {
        return base("").maxAge(0).build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(NAME, value).httpOnly(true).secure(properties.cookieSecure()).sameSite("Lax").path(PATH);
    }

}
