-- 회원 탈퇴 — 행은 지우지 않고(주문 · 결제 기록은 전자상거래법상 5년 보관) 개인정보만 지우고 탈퇴 시각을 남긴다.
ALTER TABLE member
    ADD COLUMN withdrawn_at TIMESTAMPTZ;
