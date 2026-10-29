package com.doldol.api.claim;

import java.util.EnumSet;
import java.util.Set;

public enum ClaimStatus {
    /** 접수 — 관리자 확인 전. 고객이 철회할 수 있다 */
    REQUESTED,
    /** 승인 — 반품 회수 · 교환 재발송 진행 중 */
    APPROVED,
    /** 완료 — 환불 또는 재발송 끝 */
    COMPLETED,
    /** 관리자 거절 */
    REJECTED,
    /** 고객 철회 */
    WITHDRAWN;

    /** 아직 끝나지 않은 신청 — 한 주문에 하나만 */
    public static final Set<ClaimStatus> OPEN = EnumSet.of(REQUESTED, APPROVED);
}