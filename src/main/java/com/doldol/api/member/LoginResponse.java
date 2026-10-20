package com.doldol.api.member;

public record LoginResponse(String accessToken, String tokenType, long expiresIn, MemberSummary member) {

    public record MemberSummary(Long id, String email, String name, Role role) {
    }

    public static LoginResponse of(String accessToken, long expiresIn, Member member) {
        MemberSummary summary = new MemberSummary(member.getId(), member.getEmail(), member.getName(), member.getRole());
        return new LoginResponse(accessToken, "Bearer", expiresIn, summary);
    }

}