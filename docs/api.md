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
  "createdAt": "2026-10-19T02:43:58Z"
}
```

### `POST /api/orders/{orderNumber}/cancel` 주문 취소

`PENDING_PAYMENT`, `PAID` 상태에서만 가능. 재고를 되돌립니다.

`200 OK` — `OrderDetailResponse`

---

## 나중에 추가

- 결제 (토스페이먼츠 등) — `POST /api/payments/confirm`
- 관리자 상품/주문 관리 — `/api/admin/**` (`role: ADMIN`)
- 리프레시 토큰, 배송비 정책
