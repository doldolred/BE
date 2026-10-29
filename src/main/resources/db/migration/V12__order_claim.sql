-- V12: 취소 · 반품 · 교환 신청
--   type    CANCEL(상품 준비중 취소 요청) · RETURN(반품) · EXCHANGE(교환)
--   status  REQUESTED → APPROVED → COMPLETED, 또는 REJECTED(관리자 거절) · WITHDRAWN(고객 철회)
--   취소 요청은 승인하면 바로 COMPLETED (전액 환불)
CREATE TABLE order_claim (
    id                       BIGSERIAL PRIMARY KEY,
    order_id                 BIGINT       NOT NULL REFERENCES orders (id),
    member_id                BIGINT       NOT NULL REFERENCES member (id),
    type                     VARCHAR(20)  NOT NULL,
    reason                   VARCHAR(30)  NOT NULL,
    detail                   TEXT         NOT NULL,
    status                   VARCHAR(20)  NOT NULL,
    -- 단순 변심 반품일 때 환불에서 빼는 왕복 배송비
    return_shipping_fee      INT          NOT NULL DEFAULT 0,
    -- 환불할 금액 (취소 · 반품). 교환은 0
    refund_amount            INT          NOT NULL DEFAULT 0,
    reject_reason            VARCHAR(500),
    -- 교환 재발송 송장
    exchange_courier         VARCHAR(30),
    exchange_tracking_number VARCHAR(50),
    approved_at              TIMESTAMPTZ,
    completed_at             TIMESTAMPTZ,
    created_at               TIMESTAMPTZ  NOT NULL,
    updated_at               TIMESTAMPTZ  NOT NULL
);
CREATE INDEX idx_claim_order ON order_claim (order_id);
CREATE INDEX idx_claim_member ON order_claim (member_id, created_at DESC);
CREATE INDEX idx_claim_status ON order_claim (status, created_at DESC);

-- 신청 사진 (불량 · 오배송 확인용, 최대 3장)
CREATE TABLE order_claim_image (
    claim_id    BIGINT       NOT NULL REFERENCES order_claim (id) ON DELETE CASCADE,
    sort_order  INT          NOT NULL,
    url         VARCHAR(300) NOT NULL,
    PRIMARY KEY (claim_id, sort_order)
);

-- 결제 — 환불한 금액 (반품은 배송비를 빼고 일부만 환불할 수 있다)
ALTER TABLE payment ADD COLUMN cancelled_amount INT NOT NULL DEFAULT 0;
UPDATE payment SET cancelled_amount = amount WHERE status = 'CANCELLED';
