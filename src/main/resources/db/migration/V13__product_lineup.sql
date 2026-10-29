-- 상품 상태(판매중 · 출시 예정 · 비공개) + 메인 라인업 · 상세 연출 항목.
-- active 는 남겨 status = 'SELLING' 과 같게 맞춘다 (장바구니 · 주문이 보는 값).
ALTER TABLE product
    ADD COLUMN status        VARCHAR(20)  NOT NULL DEFAULT 'SELLING'
        CHECK (status IN ('SELLING', 'COMING_SOON', 'HIDDEN')),
    ADD COLUMN en_name       VARCHAR(40),
    ADD COLUMN spec          VARCHAR(100),
    ADD COLUMN points        TEXT,
    ADD COLUMN theme         VARCHAR(20)  NOT NULL DEFAULT 'NAVY' CHECK (theme IN ('NAVY', 'PINK', 'LIME')),
    ADD COLUMN art           VARCHAR(20)  NOT NULL DEFAULT 'POUCH' CHECK (art IN ('POUCH', 'BOTTLE', 'JAR')),
    ADD COLUMN art_front_url VARCHAR(500),
    ADD COLUMN art_back_url  VARCHAR(500),
    ADD COLUMN lineup_order  INTEGER      NOT NULL DEFAULT 0;

UPDATE product SET status = CASE WHEN active THEN 'SELLING' ELSE 'HIDDEN' END;

-- 지금 파는 레드비트즙 — FE 에 박혀 있던 값 그대로 + 실제 파우치 인쇄 도안 (FE public 폴더의 정적 파일)
UPDATE product
SET en_name       = 'RED BEET',
    art_front_url = '/products/beet/pouch-art-front.webp',
    art_back_url  = '/products/beet/pouch-art-back.webp',
    spec          = '120ml × 30포 / 60포',
    points        = E'비트 100%\n물 0% · 설탕 0%\n120ml 한 포',
    theme         = 'NAVY',
    art           = 'POUCH',
    lineup_order  = 1
WHERE id = 1;

-- 출시 예정 두 가지 (옵션 없음)
INSERT INTO product (name, description, active, status, en_name, spec, points, theme, art, lineup_order)
VALUES ('돌돌 비트 농축액', '비트를 진하게 졸여 담은 농축액. 물이나 탄산수에 타서, 요리에 한 스푼.', FALSE, 'COMING_SOON',
        'BEET EXTRACT', '500ml', E'제주산 비트\n물에 타서 · 요리에\n500ml 유리병', 'PINK', 'BOTTLE', 2),
       ('돌돌 비트 파우더', '비트를 말려 곱게 간 가루. 요거트 · 스무디 · 베이킹에 붉은 색을 한 스푼.', FALSE, 'COMING_SOON',
        'BEET POWDER', '150g', E'제주산 비트 100%\n스무디 · 베이킹\n150g 유리병', 'LIME', 'JAR', 3);
