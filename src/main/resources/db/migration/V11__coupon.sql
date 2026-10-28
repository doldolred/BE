-- V11: 할인코드 — 주문서에서 코드를 넣으면 상품 금액에서 깎는다 (배송비는 그대로)
--   discount_type   AMOUNT(원) · PERCENT(%) — PERCENT 는 max_discount 로 상한
--   usage_limit     전체 사용 횟수 (비우면 무제한), per_member_limit 회원 한 명당
--   used_count      지금까지 쓴 횟수 — 주문이 취소되면 되돌린다
CREATE TABLE coupon (
    id                BIGSERIAL PRIMARY KEY,
    code              VARCHAR(30)  NOT NULL UNIQUE,
    name              VARCHAR(50)  NOT NULL,
    discount_type     VARCHAR(10)  NOT NULL,
    discount_value    INT          NOT NULL CHECK (discount_value > 0),
    max_discount      INT,
    min_order_amount  INT          NOT NULL DEFAULT 0,
    starts_at         TIMESTAMPTZ,
    ends_at           TIMESTAMPTZ,
    usage_limit       INT,
    per_member_limit  INT          NOT NULL DEFAULT 1,
    used_count        INT          NOT NULL DEFAULT 0,
    active            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL
);

-- 누가 어느 주문에 썼는지 — 주문이 취소되면 지운다 (다시 쓸 수 있게)
CREATE TABLE coupon_use (
    id               BIGSERIAL PRIMARY KEY,
    coupon_id        BIGINT       NOT NULL REFERENCES coupon (id),
    member_id        BIGINT       NOT NULL REFERENCES member (id),
    order_id         BIGINT       NOT NULL UNIQUE REFERENCES orders (id),
    discount_amount  INT          NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL
);
CREATE INDEX idx_coupon_use_member ON coupon_use (coupon_id, member_id);

-- 주문에 남기는 할인 (취소돼도 기록으로 남긴다). 결제 금액 = 상품 금액 - 할인 + 배송비
ALTER TABLE orders ADD COLUMN coupon_code VARCHAR(30);
ALTER TABLE orders ADD COLUMN discount_amount INT NOT NULL DEFAULT 0;
