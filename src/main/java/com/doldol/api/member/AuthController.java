package com.doldol.api.member;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final AuthCookies authCookies;
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse signup(@Valid @RequestBody SignupRequest request) {return authService.signup(request);}
    /** 액세스 토큰은 본문으로, 리프레시 토큰은 HttpOnly 쿠키로 */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {return withCookie(authService.login(request));}
    /** 액세스 토큰이 만료되면 FE 가 부른다 (쿠키만 있으면 됨) */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@CookieValue(name = AuthCookies.NAME, required = false) String refreshToken) {
        return withCookie(authService.refresh(refreshToken));
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = AuthCookies.NAME, required = false) String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, authCookies.clear().toString()).build();
    }
    /** 비밀번호 찾기 — 가입 여부와 관계없이 항상 204 */
    @PostMapping("/password/forgot")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {passwordResetService.requestReset(request.email());}
    @PostMapping("/password/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {passwordResetService.reset(request.token(), request.newPassword());}

    private ResponseEntity<LoginResponse> withCookie(AuthService.LoginResult result) {
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, authCookies.issue(result.refreshToken()).toString()).body(result.body());
    }
}
