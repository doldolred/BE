package com.doldol.api.member;

import com.doldol.api.oauth.SocialProvider;
import java.util.List;

/** passwordSet · socialProviders (V17) — 소셜로만 가입했으면 비밀번호 칸 대신 "비밀번호 만들기" */
public record MemberResponse(Long id, String email, String name, String phone, Role role, boolean marketingAgreed,
                             boolean passwordSet, List<SocialProvider> socialProviders) {

    public static MemberResponse from(Member member, List<SocialProvider> socialProviders) {
        return new MemberResponse(member.getId(), member.getEmail(), member.getName(), member.getPhone(),
                member.getRole(), member.isMarketingAgreed(), member.isPasswordSet(), socialProviders);
    }

}
