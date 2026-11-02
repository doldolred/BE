-- V15: 적립금 — 모든 증감을 한 줄씩 남긴다
--   type       EARN(구매확정 적립) · USE(주문에 사용) · RESTORE(취소 · 반품으로 돌려받음) · EXPIRE(소멸) · ADMIN(관리자 지급 · 차감)
--   amount     늘면 +, 줄면 -
--   remaining  적립 줄(+)만 — 아직 안 쓴 금액. 잔액 = 유효기간 안의 remaining 합
--   expires_at 적립 줄만 — 지나면 소멸
CREATE TABLE point_history (
    id          BIGSERIAL PRIMARY KEY,
    member_id   BIGINT       NOT NULL REFERENCES member (id),
    type        VARCHAR(20)  NOT NULL,
    amount      INT          NOT NULL,
    remaining   INT,
    expires_at  TIMESTAMPTZ,
    order_id    BIGINT       REFERENCES orders (id),
    memo        VARCHAR(100),
    created_at  TIMESTAMPTZ  NOT NULL
);
CREATE INDEX idx_point_member ON point_history (member_id, created_at DESC);
CREATE INDEX idx_point_usable ON point_history (member_id, expires_at) WHERE remaining > 0;
CREATE INDEX idx_point_order ON point_history (order_id);

-- 주문 — 쓴 적립금 · 확정 때 적립된 금액 · 구매확정 시각
-- 결제 금액 = 상품 금액 - 할인 - 적립금 + 배송비
ALTER TABLE orders ADD COLUMN points_used INT NOT NULL DEFAULT 0;
ALTER TABLE orders ADD COLUMN points_earned INT NOT NULL DEFAULT 0;
ALTER TABLE orders ADD COLUMN confirmed_at TIMESTAMPTZ;
