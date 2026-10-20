package com.doldol.api.member;

public record SignupResponse(Long id, String email, String name) {
    public static SignupResponse from(Member member){
        return new SignupResponse(member.getId(), member.getEmail(), member.getName());
    }
}
