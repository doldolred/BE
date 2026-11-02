package com.doldol.api.point;

import com.doldol.api.common.PageResponse;
import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.member.Member;
import com.doldol.api.member.MemberRepository;
import com.doldol.api.order.Order;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class PointService {

    private static final Sort LATEST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    private final PointHistoryRepository historyRepository;
    private final MemberRepository memberRepository;
    private final int earnRate;
    private final int minUse;
    private final int minPayment;
    private final int validDays;

    public PointService(PointHistoryRepository historyRepository, MemberRepository memberRepository,
                        @Value("${app.point.earn-rate:1}") int earnRate, @Value("${app.point.min-use:1000}") int minUse,
                        @Value("${app.point.min-payment:1000}") int minPayment, @Value("${app.point.valid-days:365}") int validDays) {
        this.historyRepository = historyRepository;
        this.memberRepository = memberRepository;
        this.earnRate = earnRate;
        this.minUse = minUse;
        this.minPayment = minPayment;
        this.validDays = validDays;
    }

    // ── 조회 ─────────────────────────────

    @Transactional(readOnly = true)
    public PointSummaryResponse summary(Long memberId) {
        Instant now = Instant.now();
        int balance = (int) historyRepository.sumUsable(memberId, now);
        int soon = (int) historyRepository.sumExpiring(memberId, now, now.plus(Duration.ofDays(30)));
        return new PointSummaryResponse(balance, soon, earnRate, minUse, minPayment, validDays);
    }

    @Transactional(readOnly = true)
    public PageResponse<PointHistoryResponse> history(Long memberId, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50), LATEST);
        return PageResponse.of(historyRepository.findByMemberId(memberId, pageable), PointHistoryResponse::from);
    }

    // ── 주문 ─────────────────────────────

    /** 주문에 적립금 사용 — 배송비까지 정해진 뒤에 부른다. 결제 금액이 minPayment 이상 남아야 한다 (PG 최소 금액) */
    public void use(Order order, Member member, int amount) {
        if (amount <= 0) {
            return;
        }
        if (amount < minUse) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "적립금은 %,d원부터 쓸 수 있어요.".formatted(minUse));
        }
        if (order.getTotalPrice() - amount < minPayment) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "적립금을 쓴 뒤에도 결제 금액이 %,d원 이상 남아야 해요.".formatted(minPayment));
        }
        consume(member, amount);
        order.applyPoints(amount);
        historyRepository.save(PointHistory.debit(member, PointType.USE, amount, order, null));
    }

    /** 취소 · 반품 — 쓴 적립금을 돌려준다. deduct 는 환불로 다 못 뺀 반품 배송비. 두 번 불러도 한 번만 */
    public void release(Order order, int deduct) {
        if (order.getPointsUsed() <= 0 || historyRepository.existsByOrderIdAndType(order.getId(), PointType.RESTORE)) {
            return;
        }
        int back = order.getPointsUsed() - Math.max(0, deduct);
        if (back > 0) {
            historyRepository.save(PointHistory.credit(order.getMember(), PointType.RESTORE, back, order, null, expiry()));
        }
    }

    /** 구매확정 적립 — 결제 금액(배송비 제외)의 earnRate %. 적립한 금액을 돌려준다 */
    public int earn(Order order) {
        int base = Math.max(0, order.getTotalPrice() - order.getShippingFee());
        int amount = base * earnRate / 100;
        if (amount > 0) {
            historyRepository.save(PointHistory.credit(order.getMember(), PointType.EARN, amount, order, null, expiry()));
        }
        return amount;
    }

    // ── 관리자 ───────────────────────────

    /** 관리자 지급(+) · 차감(-). 사유는 회원 내역에 그대로 보인다 */
    public PointSummaryResponse adjust(Long memberId, int amount, String memo) {
        if (amount == 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "0원은 지급 · 차감할 수 없어요.");
        }
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        if (amount > 0) {
            historyRepository.save(PointHistory.credit(member, PointType.ADMIN, amount, null, memo, expiry()));
        }
        else {
            consume(member, -amount);
            historyRepository.save(PointHistory.debit(member, PointType.ADMIN, -amount, null, memo));
        }
        return summary(memberId);
    }

    // ── 소멸 ─────────────────────────────

    /** 매일 0시 10분 — 유효기간이 지난 적립금을 소멸 처리 */
    @Scheduled(cron = "${app.point.expire-cron:0 10 0 * * *}", zone = "Asia/Seoul")
    public void expire() {
        List<PointHistory> lots = historyRepository.findExpired(Instant.now());
        for (PointHistory lot : lots) {
            int rest = lot.getRemaining();
            lot.take(rest);
            historyRepository.save(PointHistory.debit(lot.getMember(), PointType.EXPIRE, rest, null, null));
        }
        if (!lots.isEmpty()) {
            log.info("적립금 소멸 {}건", lots.size());
        }
    }

    // ── 도우미 ───────────────────────────

    /** 먼저 소멸할 적립 줄부터 깎는다. 모자라면 아무것도 깎지 않고 예외 */
    private void consume(Member member, int amount) {
        List<PointHistory> lots = historyRepository.findUsableForUpdate(member.getId(), Instant.now());
        int total = lots.stream().mapToInt(PointHistory::getRemaining).sum();
        if (total < amount) {
            throw new BusinessException(ErrorCode.POINT_NOT_ENOUGH, "적립금이 부족해요. (쓸 수 있는 적립금 %,d원)".formatted(total));
        }
        int left = amount;
        for (PointHistory lot : lots) {
            int take = Math.min(left, lot.getRemaining());
            lot.take(take);
            left -= take;
            if (left == 0) {
                break;
            }
        }
    }

    private Instant expiry() {
        return Instant.now().plus(Duration.ofDays(validDays));
    }

}