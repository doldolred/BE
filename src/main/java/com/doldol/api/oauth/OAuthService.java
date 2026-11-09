package com.doldol.api.oauth;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.member.AuthService;
import com.doldol.api.member.Member;
import com.doldol.api.member.MemberRepository;
import com.doldol.api.security.JwtTokenProvider;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 카카오 · 네이버 로그인 (V17).
 * 이미 연결된 소셜 계정이면 바로 로그인, 소셜이 확인한 이메일과 같은 회원이 있으면 연결하고 로그인,
 * 처음이면 약관 동의 화면으로 보낸다 (가입 토큰 10분).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OAuthService {

    private static final String SIGNUP_PURPOSE = "social-signup";
    private static final Duration SIGNUP_TTL = Duration.ofMinutes(10);

    private final OAuthProperties properties;
    private final OAuthClient oauthClient;
    private final SocialAccountRepository socialAccountRepository;
    private final MemberRepository memberRepository;
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /** 로그인 버튼 — 키를 넣은 제공자만 */
    public record ProviderInfo(SocialProvider provider, String authorizeUrl) {}

    /** 처음 온 소셜 계정 — 약관 동의 화면에 채워 줄 값 + 그 화면이 다시 보낼 가입 토큰 */
    public record SignupInfo(String signupToken, SocialProvider provider, String email, String name, String phone) {}

    /** 콜백 결과 — login(바로 로그인) 이나 signup(가입 화면으로) 둘 중 하나 */
    public record CallbackResult(AuthService.LoginResult login, SignupInfo signup) {}

    @Transactional(readOnly = true)
    public List<ProviderInfo> providers() {
        return Arrays.stream(SocialProvider.values())
                .filter(p -> properties.of(p).enabled())
                .map(p -> new ProviderInfo(p, oauthClient.authorizeUrl(p)))
                .toList();
    }

    public CallbackResult callback(SocialProvider provider, String code, String state) {
        if (!properties.of(provider).enabled()) {
            throw new BusinessException(ErrorCode.OAUTH_NOT_ENABLED);
        }
        OAuthClient.Profile profile = oauthClient.fetchProfile(provider, code, state);
        Optional<SocialAccount> linked = socialAccountRepository.findByProviderAndProviderUserId(provider, profile.id());
        if (linked.isPresent()) {
            return new CallbackResult(authService.issue(linked.get().getMember()), null);
        }
        if (profile.emailVerified() && profile.email() != null) {
            Optional<Member> same = memberRepository.findByEmail(AuthService.normalize(profile.email()));
            if (same.isPresent()) {
                socialAccountRepository.save(SocialAccount.link(same.get(), provider, profile.id()));
                return new CallbackResult(authService.issue(same.get()), null);
            }
        }
        return new CallbackResult(null, new SignupInfo(signupToken(profile), provider, profile.email(), profile.name(), profile.phone()));
    }

    /** 약관 동의 화면에서 가입 + 로그인. 비밀번호는 무작위 값 (나중에 비밀번호 찾기 · 변경으로 정할 수 있다) */
    public AuthService.LoginResult signup(SocialSignupRequest request) {
        Map<String, String> claims = jwtTokenProvider.parsePurposeToken(SIGNUP_PURPOSE, request.signupToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.OAUTH_SIGNUP_EXPIRED));
        SocialProvider provider = SocialProvider.valueOf(claims.get("provider"));
        String providerUserId = claims.get("providerUserId");
        if (socialAccountRepository.existsByProviderAndProviderUserId(provider, providerUserId)) {
            throw new BusinessException(ErrorCode.OAUTH_SIGNUP_EXPIRED); // 이미 가입됨 — 다시 로그인하면 된다
        }
        String email = AuthService.normalize(request.email());
        if (memberRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL, "이미 가입된 이메일이에요. 그 이메일로 로그인해 주세요.");
        }
        String phone = request.phone() == null || request.phone().isBlank() ? null : request.phone().trim();
        Member member = memberRepository.save(Member.createSocial(email, passwordEncoder.encode(UUID.randomUUID().toString()),
                request.name().trim(), phone, Boolean.TRUE.equals(request.agreeMarketing())));
        socialAccountRepository.save(SocialAccount.link(member, provider, providerUserId));
        return authService.issue(member);
    }

    private String signupToken(OAuthClient.Profile profile) {
        return jwtTokenProvider.createPurposeToken(SIGNUP_PURPOSE,
                Map.of("provider", profile.provider().name(), "providerUserId", profile.id()), SIGNUP_TTL);
    }

}