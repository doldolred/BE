package com.doldol.api.admin.order;

public record MemberOrderStat(Long memberId, Long orderCount, Long totalSpent) {
    public static final MemberOrderStat EMPTY = new MemberOrderStat(null, 0L, 0L);
}
