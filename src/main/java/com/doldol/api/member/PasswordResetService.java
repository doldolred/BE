package com.doldol.api.member;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.mail.MailService;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

/** 비밀번호 찾기 — 메일로 재설정 링크(30분, 한 번만) → 새 비밀번호 */
@Slf4j
@Service
@Transactional
public class PasswordResetService {

    private static final Duration VALIDITY = Duration.ofMinutes(30);

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    private final RefreshTokenService refreshTokenService;

    private final MailService mailService;

    private final String frontendUrl;

    public PasswordResetService(PasswordResetTokenRepository passwordResetTokenRepository, MemberRepository memberRepository,
            PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService, MailService mailService,
            @Value("${app.frontend-url}") String frontendUrl) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.mailService = mailService;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
    }

    /** 가입된 이메일이든 아니든 똑같이 응답한다 — 어떤 이메일이 가입돼 있는지 알아낼 수 없게 */
    public void requestReset(String email) {
        memberRepository.findByEmail(AuthService.normalize(email)).ifPresent(member -> {
            passwordResetTokenRepository.deleteUnused(member.getId());
            String token = Tokens.newToken();
            passwordResetTokenRepository.save(PasswordResetToken.create(member, Tokens.hash(token), Instant.now().plus(VALIDITY)));
            String link = UriComponentsBuilder.fromUriString(frontendUrl + "/password/reset").queryParam("token", token).build().toUriString();
            mailService.send(member.getEmail(), "[돌돌] 비밀번호 재설정 안내", """
                    %s님, 안녕하세요. 돌돌입니다.

                    아래 링크에서 새 비밀번호를 정해 주세요. 링크는 30분 동안 한 번만 쓸 수 있어요.
                    %s

                    직접 요청하지 않으셨다면 이 메일은 무시하셔도 됩니다. 비밀번호는 바뀌지 않아요.
                    """.formatted(member.getName(), link));
        });
    }

    public void reset(String token, String newPassword) {
        PasswordResetToken saved = passwordResetTokenRepository.findByTokenHash(Tokens.hash(token))
            .filter(PasswordResetToken::isUsable)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_RESET_TOKEN));
        saved.use();
        Member member = saved.getMember();
        member.changePassword(passwordEncoder.encode(newPassword));
        refreshTokenService.revokeAll(member.getId()); // 모든 기기에서 로그아웃
        log.info("비밀번호 재설정 memberId={}", member.getId());
    }

}
