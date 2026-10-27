-- V7: 공지사항 — 관리자가 쓰고, 공개(published)된 것만 손님에게 보인다. 고정(pinned)은 목록 맨 위
CREATE TABLE notice (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(100) NOT NULL,
    content     TEXT         NOT NULL,
    pinned      BOOLEAN      NOT NULL DEFAULT FALSE,
    published   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL
);

-- 손님 목록: 공개만, 고정 먼저 → 최신순
CREATE INDEX idx_notice_public ON notice (published, pinned DESC, created_at DESC);
