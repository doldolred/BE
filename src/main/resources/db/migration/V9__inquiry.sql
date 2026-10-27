-- V9: 1:1 문의 — 회원이 묻고 관리자가 답한다. 답이 달리면 status = ANSWERED (회원은 그 전까지만 지울 수 있다)
CREATE TABLE inquiry (
    id            BIGSERIAL PRIMARY KEY,
    member_id     BIGINT       NOT NULL REFERENCES member (id),
    category      VARCHAR(20)  NOT NULL,
    title         VARCHAR(100) NOT NULL,
    content       TEXT         NOT NULL,
    -- 관련 주문 (선택). 본인 주문인지는 쓸 때 확인한다
    order_number  VARCHAR(30),
    status        VARCHAR(20)  NOT NULL,
    answer        TEXT,
    answered_at   TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL
);

-- 내 문의 목록 (최신순)
CREATE INDEX idx_inquiry_member ON inquiry (member_id, created_at DESC);
-- 관리자: 답변 대기 먼저 보기
CREATE INDEX idx_inquiry_status ON inquiry (status, created_at DESC);
