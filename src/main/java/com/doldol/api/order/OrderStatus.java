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
    CANCELLED;

    public static final Set<OrderStatus> SALES = Collections.unmodifiableSet(EnumSet.of(PAID, PREPARING, SHIPPING, DELIVERED));
    public boolean isCancellable() {return this == PENDING_PAYMENT || this == PAID;}
    public boolean canMoveTo(OrderStatus next) {
        return switch (this) {
            case PENDING_PAYMENT -> next == PAID || next == CANCELLED;
            case PAID -> next == PREPARING || next == CANCELLED;
            case PREPARING -> next == SHIPPING || next == CANCELLED;
            case SHIPPING -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

}