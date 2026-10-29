package com.doldol.api.admin.claim;

import jakarta.validation.constraints.Size;

/** 완료 처리 — 교환일 때만 재발송 택배사 · 송장번호가 필요하다 (반품은 비워도 된다) */
public record ClaimCompleteRequest(
        @Size(max = 30, message = "택배사 이름이 너무 깁니다.")
        String courier,
        @Size(max = 50, message = "송장번호가 너무 깁니다.")
        String trackingNumber) {

}
