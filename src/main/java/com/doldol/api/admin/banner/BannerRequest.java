package com.doldol.api.admin.banner;

import com.doldol.api.banner.BannerTheme;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record BannerRequest(
        @NotBlank(message = "문구를 입력해 주세요.")
        @Size(max = 80, message = "문구는 80자 이하로 입력해 주세요.")
        String message,
        @Size(max = 300, message = "링크는 300자 이하로 입력해 주세요.")
        @Pattern(regexp = "^\\s*$|^\\s*/(?!/)\\S*\\s*$|^\\s*https://\\S+\\s*$", message = "링크는 /로 시작하는 사이트 안 경로나 https:// 주소만 넣을 수 있어요.")
        String linkUrl,
        @NotNull(message = "색을 골라 주세요.")
        BannerTheme theme,
        Instant startsAt,
        Instant endsAt,
        @NotNull(message = "노출 여부를 골라 주세요.")
        Boolean active,
        @NotNull(message = "순서를 입력해 주세요.")
        @Min(value = 0, message = "순서는 0 이상이에요.")
        @Max(value = 999, message = "순서는 999 이하예요.")
        Integer sortOrder) {

}