-- 1차 수정 요청: 비트 농축액 · 비트 파우더 상세 페이지 바탕도 레드비트즙과 같은 블랙(NAVY 묶음)으로 통일.
-- 관리자 상품 화면에서 색을 다시 고르면 그 값이 우선한다.
UPDATE product
SET theme = 'NAVY'
WHERE theme <> 'NAVY';
