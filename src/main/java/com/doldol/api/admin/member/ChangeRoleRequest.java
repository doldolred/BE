package com.doldol.api.admin.member;

import com.doldol.api.member.Role;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(@NotNull(message = "권한을 선택해 주세요.")Role role) {
}
