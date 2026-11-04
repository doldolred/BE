package com.doldol.api.order;

import com.doldol.api.claim.ClaimStatus;
import com.doldol.api.claim.ClaimType;
import com.doldol.api.claim.OrderClaimRepository;
import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import com.doldol.api.point.PointService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/** 구매확정 — 손님이 누르거나, 배송 완료 후 반품 기간이 지나면 자동으로. 확정하면 적립금이 들어온다 */
@Slf4j
@Service
public class PurchaseConfirmService {

    private final OrderRepository orderRepository;
    private final OrderClaimRepository claimRepository;
    private final PointService pointService;
    private final TransactionTemplate tx;
    private final Duration returnPeriod;

    public PurchaseConfirmService(OrderRepository orderRepository, OrderClaimRepository claimRepository, PointService pointService,
                                  TransactionTemplate tx, @Value("${app.claim.return-days:7}") int returnDays) {
        this.orderRepository = orderRepository;
        this.claimRepository = claimRepository;
        this.pointService = pointService;
        this.tx = tx;
        this.returnPeriod = Duration.ofDays(returnDays);
    }

    @Transactional
    public OrderDetailResponse confirm(Long memberId, String orderNumber) {
        Order order = orderRepository.findForUpdate(orderNumber)
                .filter(o -> o.getMember().getId().equals(memberId))
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        doConfirm(order);
        return OrderDetailResponse.from(order);
    }

    /** 매시 정각 — 배송 완료 후 반품 기간이 지났고 진행 중인 신청이 없는 주문을 자동 확정 */
    @Scheduled(cron = "${app.point.confirm-cron:0 0 * * * *}", zone = "Asia/Seoul")
    public void autoConfirm() {
        Instant before = Instant.now().minus(returnPeriod);
        List<String> orderNumbers = tx.execute(s -> orderRepository.findConfirmTargets(OrderStatus.DELIVERED, before, ClaimStatus.OPEN, ClaimType.EXCHANGE));
        if (orderNumbers == null || orderNumbers.isEmpty()) {
            return;
        }
        for (String orderNumber : orderNumbers) {
            try {
                tx.executeWithoutResult(s -> orderRepository.findForUpdate(orderNumber).ifPresent(o -> {
                    if (o.isConfirmable()) {
                        doConfirm(o);
                    }
                }));
            }
            catch (RuntimeException e) {
                log.error("자동 구매확정 실패 orderNumber={}", orderNumber, e);
            }
        }
        log.info("자동 구매확정 {}건", orderNumbers.size());
    }

    private void doConfirm(Order order) {
        if (!order.isConfirmable()) {
            throw new BusinessException(ErrorCode.ORDER_NOT_CONFIRMABLE);
        }
        if (claimRepository.existsByOrderIdAndStatusIn(order.getId(), ClaimStatus.OPEN)) {
            throw new BusinessException(ErrorCode.CLAIM_IN_PROGRESS);
        }
        order.confirm(pointService.earn(order));
    }

}