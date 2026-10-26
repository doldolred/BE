package com.doldol.api.admin;

import com.doldol.api.member.MemberRepository;
import com.doldol.api.member.Role;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 첫 관리자 지정 — 서버가 뜰 때 INITIAL_ADMIN_EMAILS(쉼표로 여러 개)에 적힌 회원을 관리자로 바꾼다.
 * 순서: 사이트에서 그 이메일로 먼저 가입 → .env 에 적기 → 서버 재시작. 가입 전이면 건너뛴다.
 * (가입하면 바로 관리자가 되게 하지 않는 이유: 다른 사람이 그 이메일로 먼저 가입할 수 있어서)
 */
@Slf4j
@Component
public class AdminBootstrap implements ApplicationRunner {

    private final MemberRepository memberRepository;

    private final List<String> emails;

    public AdminBootstrap(MemberRepository memberRepository, @Value("${app.admin.initial-emails:}") List<String> emails) {
        this.memberRepository = memberRepository;
        this.emails = emails;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String raw : emails) {
            String email = raw.trim().toLowerCase(Locale.ROOT);
            if (email.isEmpty()) {
                continue;
            }
            memberRepository.findByEmail(email).ifPresentOrElse(member -> {
                if (member.getRole() != Role.ADMIN) {
                    member.changeRole(Role.ADMIN);
                    log.info("관리자로 지정: {}", email);
                }
            }, () -> log.warn("INITIAL_ADMIN_EMAILS 의 {} 는 아직 가입하지 않은 이메일입니다 — 가입 후 다시 시작하세요", email));
        }
    }

}
