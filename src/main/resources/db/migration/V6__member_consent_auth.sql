-- 가입 동의 기록 + 로그인 유지(리프레시 토큰) + 비밀번호 찾기.

-- 동의 시각. 이 기능 전에 가입한 회원은 비어 있다
ALTER TABLE member
    ADD COLUMN terms_agreed_at     TIMESTAMPTZ,
    ADD COLUMN privacy_agreed_at   TIMESTAMPTZ,
    ADD COLUMN marketing_agreed    BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN marketing_agreed_at TIMESTAMPTZ;   -- 마지막으로 동의 / 철회한 시각

-- 리프레시 토큰: 원문은 쿠키에만, DB 에는 SHA-256 해시만. 쓸 때마다 새 토큰으로 바꾼다
CREATE TABLE refresh_token (
    id          BIGSERIAL PRIMARY KEY,
    member_id   BIGINT       NOT NULL REFERENCES member (id) ON DELETE CASCADE,
    token_hash  VARCHAR(64)  NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_token_member ON refresh_token (member_id);

-- 비밀번호 재설정 링크 토큰 (30분, 한 번만)
CREATE TABLE password_reset_token (
    id          BIGSERIAL PRIMARY KEY,
    member_id   BIGINT       NOT NULL REFERENCES member (id) ON DELETE CASCADE,
    token_hash  VARCHAR(64)  NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ  NOT NULL,
    used_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_password_reset_member ON password_reset_token (member_id);
