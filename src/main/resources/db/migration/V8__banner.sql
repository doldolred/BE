-- V8: 띠배너 — 쇼핑몰 맨 위 한 줄 문구 (예: "5만원 이상 무료배송"). 여러 개면 순서대로 돌아가며 보인다
--   theme     RED · BLACK · WHITE (브랜드 색 안에서만)
--   starts_at · ends_at  비우면 기간 제한 없음. 손님에게는 active 이면서 지금이 기간 안인 것만
CREATE TABLE banner (
    id          BIGSERIAL PRIMARY KEY,
    message     VARCHAR(80)  NOT NULL,
    link_url    VARCHAR(300),
    theme       VARCHAR(10)  NOT NULL,
    starts_at   TIMESTAMPTZ,
    ends_at     TIMESTAMPTZ,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    sort_order  INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL
);

-- 처음 한 줄 — 관리자 화면에서 바꾸거나 끌 수 있다
INSERT INTO banner (message, link_url, theme, active, sort_order, created_at, updated_at)
VALUES ('5만원 이상 구매 시 무료배송', NULL, 'RED', TRUE, 0, now(), now());
