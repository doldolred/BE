package com.doldol.api.member;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.cart.CartItemRepository;
import com.doldol.api.order.OrderRepository;
import com.doldol.api.order.OrderStatus;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    /** 이 상태의 주문이 있으면 탈퇴할 수 없다 (배송 · 환불을 마쳐야 하니까) */
    private static final Set<OrderStatus> ACTIVE_ORDER = EnumSet.of(OrderStatus.PENDING_PAYMENT, OrderStatus.PAID, OrderStatus.PREPARING, OrderStatus.SHIPPING);

    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    public MemberResponse getMe(Long memberId){
        return MemberResponse.from(find(memberId));
    }
    /** 마케팅 수신 동의 / 철회 */
    @Transactional
    public MemberResponse changeMarketing(Long memberId, boolean agreed){
        Member member = find(memberId);
        member.changeMarketingAgreed(agreed);
        return MemberResponse.from(member);
    }
    /**
     * 회원 탈퇴 — 비밀번호를 한 번 더 확인하고, 진행 중인 주문이 없을 때만.
     * 장바구니 · 로그인 유지 · 비밀번호 재설정 링크는 지우고, 회원 행은 개인정보만 지운다 (주문 기록 보관).
     */
    @Transactional
    public void withdraw(Long memberId, String password){
        Member member = find(memberId);
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }
        if (orderRepository.existsByMemberIdAndStatusIn(memberId, ACTIVE_ORDER)) {
            throw new BusinessException(ErrorCode.MEMBER_HAS_ACTIVE_ORDERS);
        }
        cartItemRepository.deleteAllByMemberId(memberId);
        refreshTokenRepository.deleteAllByMemberId(memberId);
        passwordResetTokenRepository.deleteUnused(memberId);
        member.withdraw(passwordEncoder.encode(UUID.randomUUID().toString()));
    }
    private Member find(Long memberId){
        return memberRepository.findById(memberId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
