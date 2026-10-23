package com.doldol.api.admin.member;

import com.doldol.api.order.OrderSummaryResponse;

import java.util.List;

public record AdminMemberDetailResponse(AdminMemberResponse member, List<OrderSummaryResponse> recentOrders) {
}
