package com.doldol.api.admin.member;

import com.doldol.api.admin.order.MemberOrderStat;
import com.doldol.api.member.Member;
import com.doldol.api.member.Role;

import java.time.Instant;

public record AdminMemberResponse(Long id, String email, String name, String phone, Role role, Instant createdAt, long orderCount, long totalSpent) {
    public static AdminMemberResponse of(Member m, MemberOrderStat stat){
        return new AdminMemberResponse(m.getId(), m.getEmail(), m.getName(), m.getPhone(), m.getRole(), m.getCreatedAt(), stat.orderCount(), stat.totalSpent());
    }
}
