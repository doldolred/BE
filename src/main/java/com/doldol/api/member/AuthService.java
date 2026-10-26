package com.doldol.api.member;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.security.JwtTokenProvider;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    /** 응답 본문(액세스 토큰) + 쿠키에 넣을 리프레시 토큰 */
    public record LoginResult(LoginResponse body, String refreshToken) {
    }

    @Transactional
    public SignupResponse signup(SignupRequest request){
        String email = normalize(request.email());
        if(memberRepository.existsByEmail(email)){
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        Member member = Member.create(email, passwordEncoder.encode(request.password()),request.name().trim(), request.phone(), Boolean.TRUE.equals(request.agreeMarketing()));
        memberRepository.save(member);
        return SignupResponse.from(member);
    }
    @Transactional
    public LoginResult login(LoginRequest request) {
        Member member = memberRepository.findByEmail(normalize(request.email())).filter(m -> passwordEncoder.matches(request.password(), m.getPassword())).orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        return issue(member);
    }
    /** 리프레시 토큰으로 새 액세스 토큰 (리프레시 토큰도 새로) */
    @Transactional
    public LoginResult refresh(String refreshToken) {
        RefreshTokenService.Rotated rotated = refreshTokenService.rotate(refreshToken);
        Member member = rotated.member();
        return new LoginResult(LoginResponse.of(jwtTokenProvider.createAccessToken(member), jwtTokenProvider.getValiditySeconds(), member), rotated.token());
    }
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }
    /** 로그인 상태에서 비밀번호 변경 — 다른 기기는 모두 로그아웃, 지금 기기에는 새 토큰 */
    @Transactional
    public LoginResult changePassword(Long memberId, ChangePasswordRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }
        member.changePassword(passwordEncoder.encode(request.newPassword()));
        refreshTokenService.revokeAll(memberId);
        return issue(member);
    }
    private LoginResult issue(Member member) {
        String accessToken = jwtTokenProvider.createAccessToken(member);
        return new LoginResult(LoginResponse.of(accessToken, jwtTokenProvider.getValiditySeconds(), member), refreshTokenService.issue(member));
    }
    static String normalize(String email){return email.trim().toLowerCase(Locale.ROOT);}
}
