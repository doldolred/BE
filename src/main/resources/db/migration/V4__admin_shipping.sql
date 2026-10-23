-- 관리자 주문관리: 배송 정보. 배송중으로 바꿀 때 택배사 · 송장번호를 넣는다
ALTER TABLE orders
    ADD COLUMN courier         VARCHAR(30),
    ADD COLUMN tracking_number VARCHAR(50),
    ADD COLUMN shipped_at      TIMESTAMPTZ,
    ADD COLUMN delivered_at    TIMESTAMPTZ;

-- 관리자 매출 · 주문 목록은 기간으로 조회한다
CREATE INDEX idx_orders_created ON orders (created_at);
