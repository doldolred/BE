CREATE TABLE product (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    description TEXT,
    price       INTEGER       NOT NULL CHECK (price >= 0),
    stock       INTEGER       NOT NULL DEFAULT 0 CHECK (stock >= 0),
    image_url   VARCHAR(500),
    active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

INSERT INTO product (name, description, price, stock)
VALUES ('돌돌 비트즙 30포', '국내산 비트를 착즙한 100% 비트즙 (100ml x 30포)', 39000, 100);
