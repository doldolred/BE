package com.doldol.api.security;

import com.doldol.api.member.Role;

public record LoginMember(Long id, String email, Role role) {

}