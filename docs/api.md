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
| 403 | `FORBIDDEN` | 권한 없음 |
| 404 | `MEMBER_NOT_FOUND` / `PRODUCT_NOT_FOUND` / `CART_ITEM_NOT_FOUND` / `ORDER_NOT_FOUND` | 대상 없음 |
| 409 | `DUPLICATE_EMAIL` | 이미 가입된 이메일 |
| 409 | `OUT_OF_STOCK` | 재고 부족 |
| 409 | `CART_EMPTY` | 빈 장바구니로 주문 |
| 409 | `ORDER_NOT_CANCELLABLE` | 취소할 수 없는 주문 상태 |
| 500 | `INTERNAL_ERROR` | 서버 오류 |

---

## 인증 / 회원

### `POST /api/auth/signup` 회원가입

```json
// 요청
{ "email": "a@doldol.com", "password": "password1!", "name": "홍길동", "phone": "010-1234-5678" }
```

- `email` 필수·이메일 형식, `password` 필수 8~64자, `name` 필수 50자 이하, `phone` 선택

`201 Created`
```json
{ "id": 1, "email": "a@doldol.com", "name": "홍길동" }
```

### `POST /api/auth/login` 로그인

```json
// 요청
{ "email": "a@doldol.com", "password": "password1!" }
```

`200 OK`
```json
{
  "accessToken": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "expiresIn": 7200,
  "member": { "id": 1, "email": "a@doldol.com", "name": "홍길동", "role": "USER" }
}
```

`expiresIn`은 초 단위입니다.

### 🔒 `GET /api/members/me` 내 정보

`200 OK`
```json
{ "id": 1, "email": "a@doldol.com", "name": "홍길동", "phone": "010-1234-5678", "role": "USER" }
```

---

## 상품

### `GET /api/products` 상품 목록 (판매중만)

`200 OK`
```json
[
  { "id": 1, "name": "돌돌 비트즙 30포", "description": "...", "price": 39000, "stock": 100, "imageUrl": null }
]
```

### `GET /api/products/{id}` 상품 상세

`200 OK` — 목록 항목과 같은 형태

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
      "productName": "돌돌 비트즙 30포",
      "imageUrl": null,
      "unitPrice": 39000,
      "quantity": 2,
      "lineTotal": 78000
    }
  ],
  "totalPrice": 78000
}
```

| API | 요청 | 설명 |
| --- | --- | --- |
| `GET /api/cart` | | 내 장바구니 |
| `POST /api/cart/items` | `{ "productId": 1, "quantity": 1 }` | 담기. 이미 있으면 수량 더함 |
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

장바구니 전체를 주문합니다. 성공하면 재고가 차감되고 장바구니가 비워집니다.

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
    "itemSummary": "돌돌 비트즙 30포 외 1건",
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
  "totalPrice": 78000,
  "receiverName": "홍길동",
  "receiverPhone": "010-1234-5678",
  "zipcode": "06236",
  "address1": "서울 강남구 테헤란로 123",
  "address2": "4층",
  "memo": "문 앞에 놓아주세요",
  "items": [
    { "productId": 1, "productName": "돌돌 비트즙 30포", "unitPrice": 39000, "quantity": 2, "lineTotal": 78000 }
  ],
  "payment": null,
  "createdAt": "2026-10-19T02:43:58Z"
}
```

`payment` 는 결제가 승인된 뒤에 채워집니다 (아래 결제 절 참고). 결제 전 · 결제 연동 전에는 `null`.

```json
"payment": {
  "provider": "NICEPAY",
  "method": "card",
  "amount": 78000,
  "approvedAt": "2026-10-19T02:45:10Z",
  "receiptUrl": "https://..."
}
```

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

### B. 포트원 V2 (`portone`)

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

## 나중에 추가

- 관리자 상품/주문 관리 — `/api/admin/**` (`role: ADMIN`)
- 리프레시 토큰, 배송비 정책
