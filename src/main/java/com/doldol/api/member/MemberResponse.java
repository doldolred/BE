package com.doldol.api.member;

public record MemberResponse(Long id, String email, String name, String phone, Role role, boolean marketingAgreed) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getEmail(), member.getName(), member.getPhone(),
                member.getRole(), member.isMarketingAgreed());
    }

}
