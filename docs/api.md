# DolDol API 명세

FE(FE 저장소의 `src/api`)와 BE가 공유하는 계약입니다. 바꿀 때는 이 문서부터 고칩니다.

- Base path: `/api`
- 형식: JSON (`Content-Type: application/json`)
- 인증: `Authorization: Bearer {accessToken}` — 🔒 표시가 있는 API는 필수
- 날짜: ISO-8601 (`2026-10-19T02:43:58Z`)
- 금액: 원 단위 정수

## 에러 응답 (공통)

```json
{
  "code": "PRODUCT_NOT_FOUND",
  "message": "상품을 찾을 수 없습니다.",
  "fieldErrors": [{ "field": "email", "message": "이메일 형식이 아닙니다." }]
}
```

`fieldErrors`는 입력값 검증 실패(400)일 때만 있습니다.

| HTTP | code | 상황 |
| --- | --- | --- |
| 400 | `INVALID_INPUT` | 요청값 검증 실패 |
| 401 | `UNAUTHORIZED` | 토큰 없음 / 만료 |
| 401 | `LOGIN_FAILED` | 이메일 또는 비밀번호 불일치 |
| 401 | `INVALID_REFRESH_TOKEN` | 로그인 유지 쿠키 없음 · 만료 · 이미 사용됨 |
| 400 | `PASSWORD_MISMATCH` | 비밀번호 변경 — 현재 비밀번호 틀림 |
| 400 | `INVALID_RESET_TOKEN` | 비밀번호 재설정 링크 만료 · 사용됨 |
| 400 | `INVALID_FILE` | 이미지가 아니거나 5MB 초과 |
| 403 | `FORBIDDEN` | 권한 없음 |
| 404 | `MEMBER_NOT_FOUND` / `PRODUCT_NOT_FOUND` / `CART_ITEM_NOT_FOUND` / `ORDER_NOT_FOUND` / `NOTICE_NOT_FOUND` / `BANNER_NOT_FOUND` / `INQUIRY_NOT_FOUND` / `REVIEW_NOT_FOUND` | 대상 없음 (공지는 비공개도) |
| 409 | `DUPLICATE_EMAIL` | 이미 가입된 이메일 |
| 409 | `OUT_OF_STOCK` | 재고 부족 |
| 409 | `CART_EMPTY` | 빈 장바구니로 주문 |
| 409 | `ORDER_NOT_CANCELLABLE` | 취소할 수 없는 주문 상태 |
| 409 | `INQUIRY_ALREADY_ANSWERED` | 답이 달린 문의 삭제 |
| 409 | `REVIEW_NOT_WRITABLE` / `REVIEW_ALREADY_WRITTEN` | 후기 — 배송 전 · 남의 상품 / 이미 씀 |
| 500 | `INTERNAL_ERROR` | 서버 오류 |

---

## 인증 / 회원

### `POST /api/auth/signup` 회원가입

```json
// 요청
{
  "email": "a@doldol.com", "password": "password1!", "name": "홍길동", "phone": "010-1234-5678",
  "agreeTerms": true, "agreePrivacy": true, "agreeAge": true, "agreeMarketing": false
}
```

- `email` 필수·이메일 형식, `password` 필수 8~64자, `name` 필수 50자 이하, `phone` 선택
- 동의: `agreeTerms`(이용약관) · `agreePrivacy`(개인정보 수집·이용) · `agreeAge`(만 14세 이상) **필수 true**, `agreeMarketing` 선택.
  동의 시각은 회원에 저장된다 (V6)

`201 Created`
```json
{ "id": 1, "email": "a@doldol.com", "name": "홍길동" }
```

### `POST /api/auth/login` 로그인

```json
// 요청
{ "email": "a@doldol.com", "password": "password1!" }
```

`200 OK` + **`Set-Cookie: doldol_rt=…; Path=/api/auth; HttpOnly; SameSite=Lax; Max-Age=1209600`** (운영은 `Secure`)
```json
{
  "accessToken": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "expiresIn": 1800,
  "member": { "id": 1, "email": "a@doldol.com", "name": "홍길동", "role": "USER" }
}
```

- 액세스 토큰은 30분(`expiresIn` 초). 로그인 유지(14일)는 리프레시 토큰 쿠키 — 자바스크립트가 읽을 수 없고 `/api/auth/**` 요청에만 실린다

### `POST /api/auth/refresh` 로그인 유지

쿠키만 있으면 된다(본문 없음). 로그인과 같은 응답 + **새 쿠키**(한 번 쓴 리프레시 토큰은 폐기).
쿠키가 없거나 만료 · 이미 사용됨 → `401 INVALID_REFRESH_TOKEN` (FE 는 로그아웃 처리)

### `POST /api/auth/logout`

이 기기의 리프레시 토큰을 지우고 쿠키를 비운다. `204`

### `POST /api/auth/password/forgot` 비밀번호 찾기

`{ "email": "a@doldol.com" }` → 항상 `204` (가입 여부를 알려 주지 않음).
가입된 이메일이면 `{FRONTEND_URL}/password/reset?token=…` 링크를 메일로 보낸다 — 30분, 한 번만. 새로 요청하면 이전 링크는 무효

### `POST /api/auth/password/reset`

`{ "token": "메일 링크의 token", "newPassword": "8~64자" }` → `204`. 모든 기기에서 로그아웃된다.
만료 · 사용된 링크 → `400 INVALID_RESET_TOKEN`

### 🔒 `GET /api/members/me` 내 정보

`200 OK`
```json
{ "id": 1, "email": "a@doldol.com", "name": "홍길동", "phone": "010-1234-5678", "role": "USER", "marketingAgreed": false }
```

### 🔒 `PATCH /api/members/me/password` 비밀번호 변경

`{ "currentPassword": "…", "newPassword": "8~64자" }` → 로그인과 같은 응답 + 새 쿠키 (다른 기기는 로그아웃).
현재 비밀번호가 틀리면 `400 PASSWORD_MISMATCH`

### 🔒 `PATCH /api/members/me/marketing` 마케팅 수신 동의 변경

`{ "agreed": true }` → `MemberResponse`

---

## 상품

가격 · 재고는 **옵션**(10포 · 30포 · 60포)마다 있다. 장바구니 · 주문은 옵션을 담는다 (V5).

### `GET /api/products` 상품 목록 (판매중만)

`200 OK`
```json
[
  {
    "id": 1,
    "name": "돌돌 제주산 100% NFC 레드비트즙",
    "description": "...",
    "imageUrl": "/uploads/products/202610/….webp",
    "minPrice": 38000,
    "soldOut": false,
    "options": [
      { "id": 1, "name": "10포", "listPrice": 16000, "price": 13900, "discountRate": 13, "stock": 0, "soldOut": true },
      { "id": 2, "name": "30포", "listPrice": 48000, "price": 38000, "discountRate": 20, "stock": 100, "soldOut": false },
      { "id": 3, "name": "60포", "listPrice": 96000, "price": 75000, "discountRate": 21, "stock": 50, "soldOut": false }
    ]
  }
]
```

- `options` 는 판매중인 옵션만, 정한 순서대로. `listPrice` 정상가 · `price` 판매가 · `discountRate` 할인율(%, 내림)
- `minPrice`: 품절 아닌 옵션 중 최저가 (다 품절이면 전체 중) — 목록에 "38,000원~"
- 상품 이미지는 `/uploads/…` (같은 도메인, 오래 캐시)

### `GET /api/products/{id}` 상품 상세

`200 OK` — 목록 항목과 같은 형태

---

## 배송비

기본 3,000원, **상품 금액 50,000원 이상 무료**. 제주 · 도서산간은 무료배송이어도 추가 (우편번호 구간 — 관리자 화면에서 관리).
처음에는 제주(63000~63644, +3,000) · 울릉도(40200~40240, +5,000)만 들어 있다.

### `GET /api/shipping/quote?itemsPrice=38000&zipcode=63100` 배송비 미리보기 (로그인 없이)

`zipcode` 는 선택(숫자 5자리)
```json
{ "baseFee": 3000, "extraFee": 3000, "areaName": "제주", "fee": 6000, "freeThreshold": 50000 }
```

---

## 공지사항 (로그인 없이)

공개(`published`)된 것만. 순서: 고정(`pinned`) 먼저 → 최신순. 본문은 일반 글(HTML 아님, 줄바꿈 그대로 보여 준다)

```json
// NoticeResponse
{ "id": 1, "title": "추석 연휴 배송 안내", "content": "9/14 ~ 9/18 출고 쉽니다.", "pinned": true, "published": true,
  "createdAt": "2026-10-27T00:05:52Z", "updatedAt": "2026-10-27T00:05:52Z" }
```

| API | 설명 |
| --- | --- |
| `GET /api/notices?page=0&size=20` | `PageResponse<NoticeResponse>` (size 최대 50) |
| `GET /api/notices/{id}` | 비공개 · 없는 공지 → `404 NOTICE_NOT_FOUND` |

---

## 띠배너 (로그인 없이)

쇼핑몰 맨 위 한 줄 문구. 여러 개면 FE 가 돌아가며 보여 준다.

### `GET /api/banners` 지금 보여 줄 배너

`active` 이고 기간 안(`startsAt` ≤ 지금 < `endsAt`, 비어 있으면 제한 없음)인 것만, `sortOrder` → `id` 순
```json
// BannerResponse
[{ "id": 1, "message": "5만원 이상 구매 시 무료배송", "linkUrl": null, "theme": "RED",
   "startsAt": null, "endsAt": null, "active": true, "sortOrder": 0 }]
```
- `theme`: `RED`(#E4002B) · `BLACK` · `WHITE`
- `linkUrl`: 사이트 안 경로(`/products/1`) 또는 `https://` 주소, 없으면 `null`(글만)

---

## 🔒 1:1 문의 (V9)

회원이 묻고 관리자가 답한다. 처음 답이 달리면 회원에게 알림 메일(`{FRONTEND_URL}/mypage/inquiries/{id}` 링크)

```json
// InquiryResponse
{ "id": 1, "category": "SHIPPING", "title": "언제 와요?", "content": "…", "orderNumber": "20261027-429918",
  "status": "WAITING", "answer": null, "answeredAt": null, "createdAt": "…" }
```
- `category`: `ORDER`(주문·결제) · `SHIPPING`(배송) · `PRODUCT`(상품) · `CANCEL`(취소·반품·교환) · `MEMBER`(회원 정보) · `ETC`(기타)
- `status`: `WAITING`(답변 대기) · `ANSWERED`(답변 완료)

| API | 요청 | 설명 |
| --- | --- | --- |
| `POST /api/inquiries` | `{ "category", "title": "100자", "content": "5,000자", "orderNumber": "선택" }` | `201`. 주문번호가 내 주문이 아니면 `404 ORDER_NOT_FOUND` |
| `GET /api/inquiries?page=0&size=20` | | 내 문의, 최신순 (`PageResponse`) |
| `GET /api/inquiries/{id}` | | 남의 문의 · 없음 → `404 INQUIRY_NOT_FOUND` |
| `DELETE /api/inquiries/{id}` | | 답 전에만 `204`, 답이 달렸으면 `409 INQUIRY_ALREADY_ANSWERED` |

---

## 상품 후기 (V10)

배송 완료(`DELIVERED`)된 내 주문 상품 하나에 후기 하나. 관리자가 숨기면 손님 목록 · 요약에서 빠진다.

```json
// ReviewResponse
{ "id": 1, "productId": 1, "productName": "돌돌 제주산 100% NFC 레드비트즙", "optionName": "30포", "rating": 5,
  "content": "…", "imageUrls": ["/uploads/reviews/202610/{uuid}.png"], "author": "홍*동",
  "best": false, "hidden": false, "reply": null, "repliedAt": null, "createdAt": "…",
  "memberId": null, "memberEmail": null }   // 손님 · 회원 화면은 이름 가림 + memberId · memberEmail 비움, 관리자는 그대로
```

| API | 로그인 | 설명 |
| --- | --- | --- |
| `GET /api/products/{id}/reviews?sort=latest&photoOnly=false&page=0&size=10` | 없이 | `sort`: `latest` · `best`(베스트 먼저) · `rating_high` · `rating_low`. `PageResponse<ReviewResponse>` |
| `GET /api/products/{id}/reviews/summary` | 없이 | `{ "total": 16, "average": 4.7, "counts": [12, 3, 1, 0, 0] }` — counts 는 5점 → 1점 |
| `POST /api/reviews/images` | 🔒 | `multipart/form-data` 파트 `file` (jpg · png · webp · gif, 5MB) → `201 { "url": "/uploads/reviews/…" }` |
| `GET /api/reviews/writable` | 🔒 | 쓸 수 있는 주문 상품 `[{ "orderItemId", "orderNumber", "productId", "productName", "optionName", "deliveredAt" }]` |
| `POST /api/reviews` | 🔒 | `{ "orderItemId": 47, "rating": 1~5, "content": "10~1,000자", "imageUrls": ["올린 주소", 최대 3장] }` → `201` |
| `GET /api/reviews/mine?page=0` | 🔒 | 내 후기 (숨김 포함) |
| `DELETE /api/reviews/{id}` | 🔒 | 내 후기 삭제 → `204`. 지우면 그 상품에 다시 쓸 수 있다 |

- 배송 전 · 남의 주문 상품 → `409 REVIEW_NOT_WRITABLE`, 이미 쓴 상품 → `409 REVIEW_ALREADY_WRITTEN`
- `imageUrls` 는 `/uploads/reviews/` 로 올린 주소만 (`400`)

---

## 🔒 장바구니

장바구니를 바꾸는 API는 모두 **장바구니 전체**(`CartResponse`)를 돌려줍니다.

```json
// CartResponse
{
  "items": [
    {
      "id": 10,
      "productId": 1,
      "productName": "돌돌 제주산 100% NFC 레드비트즙",
      "optionId": 2,
      "optionName": "30포",
      "imageUrl": null,
      "unitPrice": 38000,
      "quantity": 1,
      "lineTotal": 38000,
      "stock": 100,
      "onSale": true
    }
  ],
  "itemsPrice": 38000,
  "shippingFee": 3000,
  "freeShippingThreshold": 50000,
  "totalPrice": 41000
}
```

- `shippingFee` 는 기본 배송비 미리보기 — 제주 · 도서 추가는 주문서에서 우편번호로(`/api/shipping/quote`)
- `onSale: false` = 판매중지된 옵션 (주문 불가, 삭제 안내)

| API | 요청 | 설명 |
| --- | --- | --- |
| `GET /api/cart` | | 내 장바구니 |
| `POST /api/cart/items` | `{ "optionId": 2, "quantity": 1 }` | 담기. 이미 있으면 수량 더함. 품절이면 `409 OUT_OF_STOCK` |
| `PATCH /api/cart/items/{itemId}` | `{ "quantity": 3 }` | 수량 변경 (1 이상) |
| `DELETE /api/cart/items/{itemId}` | | 삭제 |

---

## 🔒 주문

### 주문 상태

| status | 의미 |
| --- | --- |
| `PENDING_PAYMENT` | 결제 대기 (결제 연동 전까지 주문 생성 시 이 상태) |
| `PAID` | 결제 완료 |
| `PREPARING` | 상품 준비중 |
| `SHIPPING` | 배송중 |
| `DELIVERED` | 배송 완료 |
| `CANCELLED` | 취소 |

### `POST /api/orders` 주문하기

장바구니 전체를 주문합니다. 성공하면 옵션 재고가 차감되고 장바구니가 비워집니다.
배송비는 서버가 우편번호로 계산해 `totalPrice = itemsPrice + shippingFee` 로 정한다 (결제 금액).

```json
// 요청
{
  "receiverName": "홍길동",
  "receiverPhone": "010-1234-5678",
  "zipcode": "06236",
  "address1": "서울 강남구 테헤란로 123",
  "address2": "4층",
  "memo": "문 앞에 놓아주세요"
}
```

`201 Created` — `OrderDetailResponse`

### `GET /api/orders` 내 주문 목록 (최신순)

`200 OK`
```json
[
  {
    "orderNumber": "20261019-000001",
    "status": "PENDING_PAYMENT",
    "totalPrice": 78000,
    "itemSummary": "돌돌 제주산 100% NFC 레드비트즙 30포 외 1건",
    "createdAt": "2026-10-19T02:43:58Z"
  }
]
```

### `GET /api/orders/{orderNumber}` 주문 상세

`200 OK` — `OrderDetailResponse`
```json
{
  "orderNumber": "20261019-000001",
  "status": "PENDING_PAYMENT",
  "itemsPrice": 38000,
  "shippingFee": 3000,
  "totalPrice": 41000,
  "receiverName": "홍길동",
  "receiverPhone": "010-1234-5678",
  "zipcode": "06236",
  "address1": "서울 강남구 테헤란로 123",
  "address2": "4층",
  "memo": "문 앞에 놓아주세요",
  "items": [
    { "productId": 1, "productName": "돌돌 제주산 100% NFC 레드비트즙", "optionId": 2, "optionName": "30포", "unitPrice": 38000, "quantity": 1, "lineTotal": 38000 }
  ],
  "payment": null,
  "createdAt": "2026-10-19T02:43:58Z"
}
```

`payment` 는 결제가 승인된 뒤에 채워집니다 (아래 결제 절 참고). 결제 전 · 결제 연동 전에는 `null`.

```json
"payment": {
  "provider": "NICEPAY",
  "status": "PAID",
  "method": "card",
  "amount": 78000,
  "approvedAt": "2026-10-19T02:45:10Z",
  "cancelledAt": null,
  "receiptUrl": "https://..."
}
```

`status` 는 `PAID` · `CANCELLED`(주문 취소로 환불됨 — `cancelledAt` 채워짐). 승인 거절된 시도는 여기 안 보이고 DB 에만 `FAILED` 로 남습니다.

### `POST /api/orders/{orderNumber}/cancel` 주문 취소

`PENDING_PAYMENT`, `PAID` 상태에서만 가능. 재고를 되돌립니다.
`PAID` 면 PG 결제 취소(환불)도 함께 요청합니다 — PG 취소가 실패하면 주문도 취소되지 않습니다.

`200 OK` — `OrderDetailResponse`

---

## 결제

PG 는 설정 한 줄로 고릅니다 (`PAYMENT_PROVIDER`, BE · FE 같은 값).

| 값 | 방식 | 언제 |
| --- | --- | --- |
| `nicepay` | 나이스페이 START 직접 연동 | 지금 |
| `portone` | 포트원 V2 경유 (→ 나이스페이먼츠 채널) | 정산 때 갈아탈 때 |

공통 규칙:

- 결제는 항상 **주문을 먼저 만든 뒤** (`POST /api/orders` → `PENDING_PAYMENT`) 그 `orderNumber` 와 `totalPrice` 로 요청합니다.
- **금액은 서버가 검증합니다.** PG 가 알려준 금액 ≠ 주문 `totalPrice` 면 승인하지 않습니다(위변조 방지).
- 승인되면 주문은 `PAID`, `payment` 테이블에 기록(`provider`, `payment_key`, `method`, `amount`, `approved_at`).
- 결제하지 않은 주문은 `PAYMENT_PENDING_TIMEOUT`(기본 30분) 뒤 자동 취소되고 재고가 돌아옵니다.
  (`PAYMENT_EXPIRE_CHECK_INTERVAL`, 기본 1분마다 확인. 승인 직전에 자동 취소된 주문이면 승인된 결제를 바로 취소합니다)
- 고객 · 관리자가 결제된 주문을 취소하면 **결제한 PG 로** 환불합니다(설정을 바꿔도 예전 결제는 원래 PG 로). 환불이 실패하면 주문도 취소되지 않습니다(`502 PAYMENT_GATEWAY_ERROR`).
- 결제창을 닫거나 실패해도 주문은 `PENDING_PAYMENT` 로 남아 있어서, 주문 상세에서 다시 결제할 수 있습니다.

### A. 나이스페이 START (`nicepay`)

1. FE: 나이스페이 JS SDK(`https://pay.nicepay.co.kr/v1/js/`)로 결제창
   ```js
   AUTHNICE.requestPay({
     clientId,                // VITE_NICEPAY_CLIENT_ID
     method: 'card',          // card | bank ...
     orderId: orderNumber,
     amount: totalPrice,
     goodsName: '돌돌 비트즙 30포 외 1건',
     returnUrl: `${location.origin}/api/payments/nicepay/return`,
     fnError: (result) => { /* 창 닫음 · 오류 */ },
   })
   ```
2. 인증이 끝나면 **브라우저가** 나이스페이 결과를 form(`application/x-www-form-urlencoded`)으로 BE 에 POST 합니다.

### `POST /api/payments/nicepay/return` (로그인 불필요 — 브라우저 form POST)

받는 값: `authResultCode`, `authResultMsg`, `tid`, `clientId`, `orderId`, `amount`, `mallReserved`, `authToken`, `signature`

BE 가 할 일:
1. `authResultCode == "0000"` 확인
2. `orderId` 로 주문을 찾고 `PENDING_PAYMENT` 인지, `amount == totalPrice` 인지 확인
3. 승인 요청: `POST {NICEPAY_API_URL}/v1/payments/{tid}` · 본문 `{ "amount": totalPrice }`
   · 헤더 `Authorization: Basic base64(clientId:secretKey)`
4. 승인되면 주문 `PAID` + `payment` 저장. 그 뒤 DB 저장이 실패하면 **망취소**(승인 취소) 요청
5. 브라우저를 FE 로 돌려보냄 (`302`)
   - 성공: `{FRONTEND_URL}/mypage/orders/{orderNumber}?complete=1`
   - 실패: `{FRONTEND_URL}/checkout/fail?orderNumber={orderNumber}&message={사유}`

결제 취소(환불): `POST {NICEPAY_API_URL}/v1/payments/{tid}/cancel` (같은 Basic 인증). 세부 필드는 [나이스페이 매뉴얼](https://github.com/nicepayments/nicepay-manual)의 `api/cancel.md` 기준.

### B. 포트원 V2 (`portone`) — ⏸ 나중에 (정산 때 나이스페이먼츠로 갈아탈 때)

> 지금 BE 는 **나이스페이 START 만** 구현돼 있습니다(2026-10-23 결정). 아래 포트원 API 는 갈아탈 때 만들 설계입니다. FE 는 `VITE_PAYMENT_PROVIDER=nicepay` 로 둡니다.

1. FE: `@portone/browser-sdk/v2` 로 결제창
   ```js
   PortOne.requestPayment({
     storeId,                 // VITE_PORTONE_STORE_ID
     channelKey,              // VITE_PORTONE_CHANNEL_KEY (나이스페이먼츠 채널)
     paymentId: orderNumber,  // 같은 id 로 여러 번 시도 가능, 성공은 한 번
     orderName: '돌돌 비트즙 30포 외 1건',
     totalAmount: totalPrice,
     currency: 'CURRENCY_KRW',
     payMethod: 'CARD',       // CARD | TRANSFER ...
     redirectUrl: `${location.origin}/payment/portone/redirect`,  // 모바일
   })
   ```
2. PC 는 결과를 바로 돌려받고, 모바일은 `redirectUrl?paymentId=...&code=...&message=...` 로 돌아옵니다. `code` 가 있으면 실패.
3. 성공이면 FE 가 BE 에 완료를 알립니다.

### 🔒 `POST /api/payments/portone/complete`

```json
// 요청
{ "paymentId": "20261019-000001" }
```

BE 가 할 일:
1. `GET {PORTONE_API_URL}/payments/{paymentId}` · 헤더 `Authorization: PortOne {PORTONE_API_SECRET}`
2. `status == "PAID"` 이고 `amount.total == totalPrice` 인지 확인 (주문은 로그인 회원 것만)
3. 주문 `PAID` + `payment` 저장

`200 OK` — `OrderDetailResponse` · 금액 불일치 `409 PAYMENT_AMOUNT_MISMATCH` · 미결제 `409 PAYMENT_NOT_COMPLETED`

### `POST /api/payments/portone/webhook` (로그인 불필요)

포트원이 결제 상태 변경을 알려 줍니다(가상계좌 입금 · 취소 등). `PORTONE_WEBHOOK_SECRET` 으로 서명 검증 후 위 2~3 단계와 같이 처리. 사용자가 완료 전에 창을 닫아도 주문 상태가 맞춰집니다.

### 결제 관련 에러 코드 (추가)

| HTTP | code | 상황 |
| --- | --- | --- |
| 409 | `ORDER_NOT_PAYABLE` | 결제 대기 상태가 아닌 주문 |
| 409 | `PAYMENT_AMOUNT_MISMATCH` | PG 금액 ≠ 주문 금액 |
| 409 | `PAYMENT_NOT_COMPLETED` | PG 쪽 결제가 완료되지 않음 |
| 502 | `PAYMENT_GATEWAY_ERROR` | PG 승인 · 취소 API 실패 |

---

## 🔒 관리자 (`/api/admin/**`, `role: ADMIN` 만)

관리자 만들기 — 가입 후 DB 에서: `UPDATE member SET role = 'ADMIN' WHERE email = '...';` (그 뒤로는 관리자가 회원관리에서 바꿀 수 있음)

공통:
- 날짜(`from` · `to`)는 `YYYY-MM-DD`, **한국 시간 기준**, `to` 는 그날 끝까지 포함
- 페이지 응답 `PageResponse`: `{ "content": [...], "page": 0, "size": 20, "totalElements": 42, "totalPages": 3 }` (`page` 는 0 부터, `size` 최대 100)
- 일반 회원 토큰 → `403 FORBIDDEN`, 토큰 없음 → `401 UNAUTHORIZED`

### 매출관리

#### `GET /api/admin/sales/summary?from=2026-10-01&to=2026-10-31`

비우면 오늘까지 최근 30일. 최대 1년(넘거나 from > to 면 `400 INVALID_PERIOD`).

```json
{
  "from": "2026-10-01", "to": "2026-10-31",
  "totalSales": 780000,          // 결제완료 · 준비중 · 배송중 · 배송완료 주문의 금액 합
  "orderCount": 20,
  "averageOrderValue": 39000,
  "cancelledCount": 2, "cancelledAmount": 78000,
  "pendingCount": 3,             // 아직 결제 대기
  "daily": [ { "date": "2026-10-01", "sales": 39000, "orders": 1 }, ... ],   // 주문 없는 날도 0 으로
  "topProducts": [ { "productId": 1, "productName": "돌돌 비트즙 30포", "quantity": 22, "sales": 858000 } ]  // 금액순 5개
}
```

### 주문관리

#### `GET /api/admin/orders?status=&from=&to=&keyword=&page=0&size=20`

모든 조건은 선택. `keyword` 는 주문번호 · 받는 분 · 회원 이메일 · 회원 이름에서 찾음(대소문자 무시). 최신순.

`200 OK` — `PageResponse` of
```json
{ "orderNumber": "20261022-318151", "status": "PAID", "totalPrice": 78000, "itemSummary": "돌돌 비트즙 30포",
  "memberEmail": "a@doldol.com", "memberName": "홍길동", "receiverName": "홍길동", "createdAt": "2026-10-22T05:50:40Z" }
```

#### `GET /api/admin/orders/{orderNumber}`

```json
{
  "order": { /* 고객용 OrderDetailResponse 와 같음 */ },
  "memberId": 6, "memberEmail": "a@doldol.com", "memberName": "홍길동",
  "courier": "CJ대한통운", "trackingNumber": "123456789012",
  "shippedAt": "2026-10-23T01:00:00Z", "deliveredAt": null
}
```

#### `PATCH /api/admin/orders/{orderNumber}/status`

```json
{ "status": "SHIPPING", "courier": "CJ대한통운", "trackingNumber": "123456789012" }
```

| 지금 → 바꿀 수 있는 상태 | |
| --- | --- |
| `PENDING_PAYMENT` → `PAID`(결제 확인) · `CANCELLED` | |
| `PAID` → `PREPARING` · `CANCELLED` | |
| `PREPARING` → `SHIPPING`(택배사 · 송장번호 **필수**) · `CANCELLED` | |
| `SHIPPING` → `DELIVERED` | |
| `DELIVERED` · `CANCELLED` → 변경 불가 | |

- 규칙에 없는 변경 → `409 INVALID_STATUS_TRANSITION`, 송장 없이 `SHIPPING` → `400 INVALID_INPUT`
- `CANCELLED` 로 바꾸면 재고가 돌아온다. (결제 연동 후에는 PG 환불도 함께 — TODO)

`200 OK` — 위 주문 상세

### 제품관리

| API | 설명 |
| --- | --- |
| `GET /api/admin/products` | 판매 중지 포함 전체, 최근 등록순 |
| `GET /api/admin/products/{id}` | 상세 |
| `POST /api/admin/products` | 등록 → `201` |
| `PUT /api/admin/products/{id}` | 수정 (전체 값) |
| `PATCH /api/admin/products/{id}/active` `{ "active": false }` | 판매 중지 / 재개 |

```json
// 등록 · 수정 요청
{ "name": "돌돌 비트 농축액", "description": "...", "price": 29000, "stock": 50, "imageUrl": "https://...", "active": true }
// 응답
{ "id": 2, "name": "...", "description": "...", "price": 29000, "stock": 50, "imageUrl": "https://...",
  "active": true, "createdAt": "...", "updatedAt": "..." }
```

상품은 지우지 않는다 — 지난 주문이 참조하고 있어서 **판매 중지**로 숨긴다. 판매 중지 상품은 고객 목록 · 상세에서 안 보인다(404). 가격을 바꿔도 지난 주문의 금액은 그대로.

#### 상품 요청 본문 (V5 — 옵션)

```json
{
  "name": "돌돌 제주산 100% NFC 레드비트즙", "description": "…", "imageUrl": "/uploads/…", "active": true,
  "options": [
    { "id": 2, "name": "30포", "listPrice": 48000, "price": 38000, "stock": 100, "active": true },
    { "name": "90포", "listPrice": 144000, "price": 110000, "stock": 10, "active": true }
  ]
}
```

- 옵션은 **전부** 보낸다: `id` 가 있으면 수정, 없으면 추가, 목록에서 빠진 기존 옵션은 판매중지(`active=false`, 지난 주문이 가리키므로 지우지 않음). 순서 = 배열 순서
- 응답 `AdminProductResponse` 의 `options` 에는 판매중지된 옵션도 들어 있다 (`sortOrder` · `active` 포함)

#### 🔒 `POST /api/admin/uploads` 상품 이미지 올리기

`multipart/form-data`, 파트 이름 `file`. jpg · png · webp · gif, 5MB 이하 (파일 내용으로 판별, 이름은 새로 지음).
`201` `{ "url": "/uploads/products/202610/{uuid}.png" }` → 상품 `imageUrl` 에 넣는다. 서버 디스크(운영: 도커 볼륨)에 저장, 매일 백업

### 배송 지역 (추가 배송비)

| API | 설명 |
| --- | --- |
| `GET /api/admin/shipping/areas` | 목록 `[{ "id", "name", "zipFrom", "zipTo", "extraFee" }]` |
| `POST /api/admin/shipping/areas` | `{ "name": "신안군", "zipFrom": "58800", "zipTo": "58866", "extraFee": 5000 }` → `201` |
| `DELETE /api/admin/shipping/areas/{id}` | `204` |

### 공지사항 (V7)

| API | 설명 |
| --- | --- |
| `GET /api/admin/notices?page=0&size=20` | 비공개 포함 전부, 손님 화면과 같은 순서 (size 최대 100) |
| `GET /api/admin/notices/{id}` | |
| `POST /api/admin/notices` | `{ "title": "100자 이하", "content": "20,000자 이하", "pinned": false, "published": true }` → `201` |
| `PUT /api/admin/notices/{id}` | 같은 본문 → `200` |
| `DELETE /api/admin/notices/{id}` | `204` |

### 띠배너 (V8)

| API | 설명 |
| --- | --- |
| `GET /api/admin/banners` | 꺼진 것 · 기간 지난 것 포함 전부, `sortOrder` → `id` 순 |
| `POST /api/admin/banners` | `BannerRequest` → `201` |
| `PUT /api/admin/banners/{id}` | `BannerRequest` → `200` |
| `DELETE /api/admin/banners/{id}` | `204` |

```json
// BannerRequest
{ "message": "80자 이하", "linkUrl": "/products/1 | https://… | 빈 값", "theme": "RED",
  "startsAt": "2026-11-01T00:00:00Z", "endsAt": null, "active": true, "sortOrder": 0 }
```
- 링크는 `/`로 시작하는 경로(`//` 제외)나 `https://` 만 — 그 밖은 `400`
- `endsAt` 이 `startsAt` 보다 앞이거나 같으면 `400 INVALID_INPUT`
- 처음에 "5만원 이상 구매 시 무료배송"(RED) 한 줄이 들어 있다

### 문의함 (V9)

| API | 설명 |
| --- | --- |
| `GET /api/admin/inquiries?status=WAITING&page=0&size=20` | `status` 빼면 전부. 최신순. `InquiryResponse` + `memberId` · `memberEmail` · `memberName` |
| `GET /api/admin/inquiries/waiting-count` | `{ "count": 3 }` — 메뉴의 답변 대기 수 |
| `GET /api/admin/inquiries/{id}` | |
| `PUT /api/admin/inquiries/{id}/answer` | `{ "answer": "5,000자 이하" }` → 답하기 · 고치기. 알림 메일은 처음 답할 때만 |

### 후기관리 (V10)

| API | 설명 |
| --- | --- |
| `GET /api/admin/reviews?filter=ALL&page=0&size=20` | `filter`: `ALL` · `HIDDEN` · `BEST` · `NO_REPLY`. 최신순, 작성자 이름 · 이메일 그대로 |
| `PATCH /api/admin/reviews/{id}` | `{ "hidden": true, "best": true, "reply": "1,000자" }` — 보낸 것만 바꾼다. `reply: ""` 이면 답글 삭제 |

### 첫 관리자

서버를 켤 때 `INITIAL_ADMIN_EMAILS`(쉼표로 여러 개)에 적힌 **이미 가입한** 회원을 관리자로 바꾼다. 가입 → `.env` 에 적기 → 재시작.

### 회원관리

#### `GET /api/admin/members?keyword=&page=0&size=20`

`keyword` 는 이메일 · 이름(대소문자 무시). 최근 가입순. `PageResponse` of
```json
{ "id": 6, "email": "a@doldol.com", "name": "홍길동", "phone": null, "role": "USER", "createdAt": "...",
  "orderCount": 2, "totalSpent": 78000 }   // totalSpent: 매출로 잡는 주문만
```

#### `GET /api/admin/members/{id}`

```json
{ "member": { /* 위 한 줄과 같음 */ }, "recentOrders": [ /* OrderSummaryResponse, 최근 5건 */ ] }
```

#### `PATCH /api/admin/members/{id}/role` `{ "role": "ADMIN" }`

자기 자신의 권한은 못 바꾼다 → `409 CANNOT_CHANGE_OWN_ROLE`

### 관리자 에러 코드 (추가)

| HTTP | code | 상황 |
| --- | --- | --- |
| 409 | `INVALID_STATUS_TRANSITION` | 규칙에 없는 주문 상태 변경 |
| 409 | `CANNOT_CHANGE_OWN_ROLE` | 본인 권한 변경 시도 |
| 400 | `INVALID_PERIOD` | 조회 기간이 거꾸로이거나 1년 초과 |

---

## 나중에 추가

- 엑셀 내보내기, 할인코드, 취소 · 반품 · 교환, 정기구독, 회원 등급
