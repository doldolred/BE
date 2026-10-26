package com.doldol.api.member;

import com.doldol.api.security.LoginMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final AuthService authService;
    private final AuthCookies authCookies;

    @GetMapping("/me")
    public MemberResponse me(@AuthenticationPrincipal LoginMember loginMember) {
        return memberService.getMe(loginMember.id());
    }

    /** 비밀번호 변경 — 다른 기기는 로그아웃되고, 지금 기기에는 새 토큰을 준다 */
    @PatchMapping("/me/password")
    public ResponseEntity<LoginResponse> changePassword(@AuthenticationPrincipal LoginMember loginMember, @Valid @RequestBody ChangePasswordRequest request) {
        AuthService.LoginResult result = authService.changePassword(loginMember.id(), request);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, authCookies.issue(result.refreshToken()).toString()).body(result.body());
    }

    @PatchMapping("/me/marketing")
    public MemberResponse changeMarketing(@AuthenticationPrincipal LoginMember loginMember, @Valid @RequestBody MarketingConsentRequest request) {
        return memberService.changeMarketing(loginMember.id(), request.agreed());
    }

}
