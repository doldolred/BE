package com.doldol.api.order;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {

    PENDING_PAYMENT,
    PAID,
    PREPARING,
    SHIPPING,
    DELIVERED,
    CANCELLED,
    /** 반품 완료 (V12) — 반품 신청을 처리할 때만 */
    RETURNED;

    public static final Set<OrderStatus> SALES = Collections.unmodifiableSet(EnumSet.of(PAID, PREPARING, SHIPPING, DELIVERED));
    public boolean isCancellable() {return this == PENDING_PAYMENT || this == PAID;}
    public boolean canMoveTo(OrderStatus next) {
        return switch (this) {
            case PENDING_PAYMENT -> next == PAID || next == CANCELLED;
            case PAID -> next == PREPARING || next == CANCELLED;
            case PREPARING -> next == SHIPPING || next == CANCELLED;
            case SHIPPING -> next == DELIVERED;
            case DELIVERED -> next == RETURNED;
            case CANCELLED, RETURNED -> false;
        };
    }

}