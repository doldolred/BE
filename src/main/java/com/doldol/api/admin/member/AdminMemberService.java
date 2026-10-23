package com.doldol.api.admin.member;

import com.doldol.api.admin.order.AdminOrderRepository;
import com.doldol.api.admin.order.MemberOrderStat;
import com.doldol.api.common.PageResponse;
import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.member.Member;
import com.doldol.api.member.Role;
import com.doldol.api.order.OrderStatus;
import com.doldol.api.order.OrderSummaryResponse;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

    private final AdminMemberRepository adminMemberRepository;
    private final AdminOrderRepository adminOrderRepository;
    public PageResponse<AdminMemberResponse> search(String keyword, int page, int size) {
        var pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100),
                Sort.by(Sort.Direction.DESC, "id"));
        String kw = keyword == null || keyword.isBlank() ? null : keyword.trim();
        Page<Member> members = kw == null ? adminMemberRepository.findAll(pageable) : adminMemberRepository.findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(kw, kw, pageable);

        List<Long> ids = members.getContent().stream().map(Member::getId).toList();
        Map<Long, MemberOrderStat> stats = ids.isEmpty() ? Map.of() : adminOrderRepository.statsByMembers(ids, OrderStatus.SALES).stream().collect(Collectors.toMap(MemberOrderStat::memberId, Function.identity()));
        return PageResponse.of(members, m -> AdminMemberResponse.of(m, stats.getOrDefault(m.getId(), MemberOrderStat.EMPTY)));}

    public AdminMemberDetailResponse getMember(Long id) {
        Member member = find(id);
        MemberOrderStat stat = adminOrderRepository.statsByMembers(List.of(id), OrderStatus.SALES).stream().findFirst().orElse(MemberOrderStat.EMPTY);
        List<OrderSummaryResponse> recent = adminOrderRepository.findTop5ByMemberIdOrderByCreatedAtDesc(id).stream().map(OrderSummaryResponse::from).toList();
        return new AdminMemberDetailResponse(AdminMemberResponse.of(member, stat), recent);
    }

    @Transactional
    public AdminMemberDetailResponse changeRole(Long loginMemberId, Long id, Role role) {
        if (loginMemberId.equals(id)) {throw new BusinessException(ErrorCode.CANNOT_CHANGE_OWN_ROLE);}
        find(id).changeRole(role);
        return getMember(id);
    }
    private Member find(Long id) {return adminMemberRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));}

}