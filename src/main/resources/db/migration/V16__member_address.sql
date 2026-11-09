-- V16: 배송지 관리 — 회원마다 최대 10개, 그중 하나가 기본 배송지(주문서에 먼저 채워진다)
CREATE TABLE member_address (
    id              BIGSERIAL PRIMARY KEY,
    member_id       BIGINT       NOT NULL REFERENCES member (id),
    label           VARCHAR(20)  NOT NULL,
    receiver_name   VARCHAR(50)  NOT NULL,
    receiver_phone  VARCHAR(20)  NOT NULL,
    zipcode         VARCHAR(10)  NOT NULL,
    address1        VARCHAR(255) NOT NULL,
    address2        VARCHAR(255),
    is_default      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL
);
CREATE INDEX idx_member_address_member ON member_address (member_id);
