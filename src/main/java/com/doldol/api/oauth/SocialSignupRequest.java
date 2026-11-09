package com.doldol.api.oauth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 소셜 첫 로그인 → 약관 동의 화면에서 가입 (V17). 비밀번호 대신 가입 토큰 */
public record SocialSignupRequest(
        @NotBlank(message = "처음부터 다시 로그인해 주세요.")
        String signupToken,
        @NotBlank(message = "이메일을 입력해 주세요.")
        @Email(message = "이메일 형식이 아닙니다.")
        @Size(max = 255, message = "이메일이 너무 깁니다.")
        String email,
        @NotBlank(message = "이름을 입력해 주세요.")
        @Size(max = 50, message = "이름은 50자 이하로 입력해 주세요.")
        String name,
        // 선택. 010-1234-5678 / 01012345678
        @Pattern(regexp = "^$|^01[0-9]-?\\d{3,4}-?\\d{4}$", message = "휴대폰 번호 형식이 아닙니다.")
        String phone,
        @NotNull(message = "이용약관에 동의해 주세요.")
        @AssertTrue(message = "이용약관에 동의해 주세요.")
        Boolean agreeTerms,
        @NotNull(message = "개인정보 수집 · 이용에 동의해 주세요.")
        @AssertTrue(message = "개인정보 수집 · 이용에 동의해 주세요.")
        Boolean agreePrivacy,
        @NotNull(message = "만 14세 이상만 가입할 수 있습니다.")
        @AssertTrue(message = "만 14세 이상만 가입할 수 있습니다.")
        Boolean agreeAge,
        Boolean agreeMarketing) {}