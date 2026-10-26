-- 상품 옵션(10포 · 30포 · 60포) + 배송비.
-- 가격 · 재고는 상품이 아니라 옵션마다 둔다. 장바구니 · 주문은 옵션을 가리킨다.

CREATE TABLE product_option (
    id          BIGSERIAL PRIMARY KEY,
    product_id  BIGINT       NOT NULL REFERENCES product (id),
    name        VARCHAR(50)  NOT NULL,              -- 화면에 보이는 옵션 이름 (예: 30포)
    list_price  INTEGER      NOT NULL CHECK (list_price >= 0),   -- 정상가 (할인 전)
    price       INTEGER      NOT NULL CHECK (price >= 0),        -- 판매가
    stock       INTEGER      NOT NULL DEFAULT 0 CHECK (stock >= 0),
    sort_order  INTEGER      NOT NULL DEFAULT 0,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_product_option_product ON product_option (product_id, sort_order);

-- 기존 상품을 실제 상품(제주산 NFC 레드비트즙 120ml)으로 바꾸고 옵션 3개를 만든다
UPDATE product
SET name        = '돌돌 제주산 100% NFC 레드비트즙',
    description = '제주산 비트를 통째로 착즙했어요. 물 · 설탕 · 첨가물 없이 비트만 담은 120ml 파우치.'
WHERE id = 1;

INSERT INTO product_option (product_id, name, list_price, price, stock, sort_order)
SELECT id, '10포', 16000, 13900, 0, 1 FROM product WHERE id = 1;
INSERT INTO product_option (product_id, name, list_price, price, stock, sort_order)
SELECT id, '30포', 48000, 38000, stock, 2 FROM product WHERE id = 1;
INSERT INTO product_option (product_id, name, list_price, price, stock, sort_order)
SELECT id, '60포', 96000, 75000, 50, 3 FROM product WHERE id = 1;

-- 다른 상품이 있었다면 그 가격 · 재고 그대로 옵션 하나
INSERT INTO product_option (product_id, name, list_price, price, stock, sort_order)
SELECT id, '기본', price, price, stock, 1 FROM product WHERE id <> 1;

-- 장바구니: 상품 → 옵션 (기존 항목은 그 상품의 첫 판매 옵션으로)
ALTER TABLE cart_item ADD COLUMN option_id BIGINT REFERENCES product_option (id);
UPDATE cart_item c
SET option_id = (SELECT o.id FROM product_option o
                 WHERE o.product_id = c.product_id
                 ORDER BY (o.stock > 0) DESC, o.sort_order LIMIT 1);
ALTER TABLE cart_item ALTER COLUMN option_id SET NOT NULL;
ALTER TABLE cart_item DROP CONSTRAINT cart_item_member_id_product_id_key;
ALTER TABLE cart_item DROP COLUMN product_id;
ALTER TABLE cart_item ADD CONSTRAINT uq_cart_item_member_option UNIQUE (member_id, option_id);

-- 주문 상품: 어떤 옵션이었는지 남긴다 (예전 주문은 30포로 간주)
ALTER TABLE order_item
    ADD COLUMN option_id   BIGINT REFERENCES product_option (id),
    ADD COLUMN option_name VARCHAR(50);
UPDATE order_item i
SET option_id   = (SELECT o.id FROM product_option o WHERE o.product_id = i.product_id ORDER BY o.sort_order DESC LIMIT 1),
    option_name = (SELECT o.name FROM product_option o WHERE o.product_id = i.product_id ORDER BY o.sort_order DESC LIMIT 1);
UPDATE order_item i
SET option_id   = (SELECT o.id FROM product_option o WHERE o.product_id = i.product_id AND o.name = '30포'),
    option_name = '30포'
WHERE i.product_id = 1;
ALTER TABLE order_item
    ALTER COLUMN option_id SET NOT NULL,
    ALTER COLUMN option_name SET NOT NULL;

-- 가격 · 재고는 옵션으로 옮겼으니 상품에서 뺀다
ALTER TABLE product DROP COLUMN price, DROP COLUMN stock;

-- 주문 금액 = 상품 금액 + 배송비
ALTER TABLE orders
    ADD COLUMN items_price  INTEGER NOT NULL DEFAULT 0 CHECK (items_price >= 0),
    ADD COLUMN shipping_fee INTEGER NOT NULL DEFAULT 0 CHECK (shipping_fee >= 0);
UPDATE orders SET items_price = total_price;

-- 추가 배송비 지역 (우편번호 구간). 관리자 화면에서 더하고 뺀다
CREATE TABLE shipping_area (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    zip_from    VARCHAR(5)   NOT NULL,
    zip_to      VARCHAR(5)   NOT NULL,
    extra_fee   INTEGER      NOT NULL CHECK (extra_fee >= 0),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CHECK (zip_from <= zip_to)
);

INSERT INTO shipping_area (name, zip_from, zip_to, extra_fee) VALUES
    ('제주', '63000', '63644', 3000),
    ('울릉도', '40200', '40240', 5000);
