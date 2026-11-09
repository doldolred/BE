package com.doldol.api.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        /** 소셜로만 가입해 비밀번호를 정한 적이 없으면 비워도 된다 (서버가 확인) */
        String currentPassword,
        @NotBlank(message = "새 비밀번호를 입력해 주세요.")
        @Size(min = 8, max = 64, message = "비밀번호는 8~64자로 입력해 주세요.")
        String newPassword) {

}
