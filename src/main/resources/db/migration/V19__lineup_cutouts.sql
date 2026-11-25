-- 1차 수정 요청: 라인업의 비트 농축액 · 비트 파우더 그림을 고객사가 준 제품 누끼 이미지로 교체.
-- 이미지는 FE 가 정적 파일로 갖고 있다 (FE public/products/extract · powder). 용량 표기도 이미지에 적힌 값으로 맞춘다.
-- 관리자가 이미 대표 이미지를 올린 상품은 건드리지 않는다.
UPDATE product
SET image_url = '/products/extract/cutout.webp',
    spec      = '13g 스틱',
    points    = E'제주산 비트\n물에 타서 · 요리에\n13g 스틱'
WHERE name = '돌돌 비트 농축액'
  AND image_url IS NULL;

UPDATE product
SET image_url = '/products/powder/cutout.webp',
    spec      = '150g · 30회분',
    points    = E'제주산 비트 100%\n스무디 · 베이킹\n1포 5g · 30회분'
WHERE name = '돌돌 비트 파우더'
  AND image_url IS NULL;
