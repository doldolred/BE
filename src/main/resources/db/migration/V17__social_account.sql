-- V17: 카카오 · 네이버 로그인 — 소셜 계정(제공자 + 그쪽 회원 번호)을 우리 회원에 연결한다
CREATE TABLE social_account (
    id                BIGSERIAL PRIMARY KEY,
    member_id         BIGINT       NOT NULL REFERENCES member (id),
    provider          VARCHAR(20)  NOT NULL,
    provider_user_id  VARCHAR(100) NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uq_social_account UNIQUE (provider, provider_user_id)
);
CREATE INDEX idx_social_account_member ON social_account (member_id);

-- 소셜로만 가입한 회원은 비밀번호를 정한 적이 없다 (무작위 값이 들어 있음).
-- 이 값이 false 면 비밀번호 변경 · 탈퇴 때 현재 비밀번호를 묻지 않는다
ALTER TABLE member ADD COLUMN password_set BOOLEAN NOT NULL DEFAULT TRUE;
