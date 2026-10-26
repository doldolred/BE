package com.doldol.api.member;

import jakarta.validation.constraints.NotNull;

public record MarketingConsentRequest(
        @NotNull(message = "동의 여부를 선택해 주세요.")
        Boolean agreed) {

}
