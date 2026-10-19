CREATE TABLE member (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    name        VARCHAR(50)  NOT NULL,
    phone       VARCHAR(20),
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE cart_item (
    id          BIGSERIAL PRIMARY KEY,
    member_id   BIGINT       NOT NULL REFERENCES member (id),
    product_id  BIGINT       NOT NULL REFERENCES product (id),
    quantity    INTEGER      NOT NULL CHECK (quantity > 0),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (member_id, product_id)
);

-- order 는 SQL 예약어라 orders 사용
CREATE TABLE orders (
    id              BIGSERIAL PRIMARY KEY,
    order_number    VARCHAR(30)  NOT NULL UNIQUE,
    member_id       BIGINT       NOT NULL REFERENCES member (id),
    status          VARCHAR(20)  NOT NULL,
    total_price     INTEGER      NOT NULL CHECK (total_price >= 0),
    receiver_name   VARCHAR(50)  NOT NULL,
    receiver_phone  VARCHAR(20)  NOT NULL,
    zipcode         VARCHAR(10)  NOT NULL,
    address1        VARCHAR(255) NOT NULL,
    address2        VARCHAR(255),
    memo            VARCHAR(255),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_orders_member ON orders (member_id, created_at DESC);

CREATE TABLE order_item (
    id            BIGSERIAL PRIMARY KEY,
    order_id      BIGINT       NOT NULL REFERENCES orders (id),
    product_id    BIGINT       NOT NULL REFERENCES product (id),
    product_name  VARCHAR(100) NOT NULL,
    unit_price    INTEGER      NOT NULL CHECK (unit_price >= 0),
    quantity      INTEGER      NOT NULL CHECK (quantity > 0)
);

CREATE INDEX idx_order_item_order ON order_item (order_id);
