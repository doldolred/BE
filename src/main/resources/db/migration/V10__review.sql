-- V10: 상품 후기 — 배송 완료된 내 주문 상품 하나에 후기 하나 (order_item_id UNIQUE)
--   hidden  관리자가 숨김 (손님 화면 · 평점 요약에서 빠진다)
--   best    관리자가 고른 베스트 후기 (베스트순 맨 위)
--   reply   돌돌 답글 (선택)
CREATE TABLE review (
    id             BIGSERIAL PRIMARY KEY,
    member_id      BIGINT       NOT NULL REFERENCES member (id),
    product_id     BIGINT       NOT NULL REFERENCES product (id),
    order_item_id  BIGINT       NOT NULL UNIQUE REFERENCES order_item (id),
    option_name    VARCHAR(50)  NOT NULL,
    rating         INT          NOT NULL CHECK (rating BETWEEN 1 AND 5),
    content        TEXT         NOT NULL,
    hidden         BOOLEAN      NOT NULL DEFAULT FALSE,
    best           BOOLEAN      NOT NULL DEFAULT FALSE,
    reply          TEXT,
    replied_at     TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL
);

-- 후기 사진 (최대 3장, 순서대로). 후기를 지우면 같이 지워진다
CREATE TABLE review_image (
    review_id   BIGINT       NOT NULL REFERENCES review (id) ON DELETE CASCADE,
    sort_order  INT          NOT NULL,
    url         VARCHAR(300) NOT NULL,
    PRIMARY KEY (review_id, sort_order)
);

-- 상품 상세 후기 목록 · 요약
CREATE INDEX idx_review_product ON review (product_id, hidden, created_at DESC);
-- 내 후기
CREATE INDEX idx_review_member ON review (member_id, created_at DESC);
