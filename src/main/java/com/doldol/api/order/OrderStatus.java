package com.doldol.api.order;

public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    PREPARING,
    SHIPPING,
    DELIVERED,
    CANCELLED;
    public boolean isCancellable(){
        return this == PENDING_PAYMENT || this == PAID;
    }
}
