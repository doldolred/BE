-- 결제 기록. 한 주문에 시도가 여러 번 있을 수 있다(실패 후 재결제) — 그래서 order_id 는 UNIQUE 가 아니다.
-- provider: 지금은 NICEPAY(START 직접 연동), 정산 때 PORTONE(→ 나이스페이먼츠)으로 바꿀 수 있게 둘 다 담는다.
CREATE TABLE payment (
    id            BIGSERIAL PRIMARY KEY,
    order_id      BIGINT       NOT NULL REFERENCES orders (id),
    provider      VARCHAR(20)  NOT NULL,  -- NICEPAY | PORTONE
    payment_key   VARCHAR(100) NOT NULL,  -- NICEPAY: tid / PORTONE: paymentId
    method        VARCHAR(30),            -- card, bank ... (PG 가 알려준 값)
    amount        INTEGER      NOT NULL CHECK (amount >= 0),
    status        VARCHAR(20)  NOT NULL,  -- PAID | FAILED | CANCELLED
    receipt_url   VARCHAR(500),
    fail_reason   VARCHAR(255),
    approved_at   TIMESTAMPTZ,
    cancelled_at  TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (provider, payment_key)
);

CREATE INDEX idx_payment_order ON payment (order_id);

-- 결제 대기 주문을 일정 시간 뒤 자동 취소할 때 쓰는 조회용
CREATE INDEX idx_orders_status_created ON orders (status, created_at);
