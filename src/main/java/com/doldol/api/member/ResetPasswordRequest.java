package com.doldol.api.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** token = 메일 링크에 있던 값 */
public record ResetPasswordRequest(
        @NotBlank(message = "링크가 올바르지 않습니다.")
        String token,
        @NotBlank(message = "새 비밀번호를 입력해 주세요.")
        @Size(min = 8, max = 64, message = "비밀번호는 8~64자로 입력해 주세요.")
        String newPassword) {

}
