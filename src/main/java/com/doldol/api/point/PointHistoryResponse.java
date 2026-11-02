package com.doldol.api.point;

import java.time.Instant;

public record PointHistoryResponse(Long id, PointType type, int amount, String orderNumber, String memo, Instant expiresAt, Instant createdAt) {
    public static PointHistoryResponse from(PointHistory p) {return new PointHistoryResponse(p.getId(), p.getType(), p.getAmount(), p.getOrder() == null ? null : p.getOrder().getOrderNumber(), p.getMemo(), p.getExpiresAt(), p.getCreatedAt());}
}