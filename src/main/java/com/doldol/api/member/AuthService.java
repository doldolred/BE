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
    @Transactional
    public SignupResponse signup(SignupRequest request){
        String email = normalize(request.email());
        if(memberRepository.existsByEmail(email)){
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        Member member = Member.create(email, passwordEncoder.encode(request.password()),request.name().trim(), request.phone());
        memberRepository.save(member);
        return SignupResponse.from(member);
    }
    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(normalize(request.email())).filter(m -> passwordEncoder.matches(request.password(), m.getPassword())).orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        String accessToken = jwtTokenProvider.createAccessToken(member);
        return LoginResponse.of(accessToken, jwtTokenProvider.getValiditySeconds(), member);
    }
    private String normalize(String email){return email.trim().toLowerCase(Locale.ROOT);}
}
