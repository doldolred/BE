package com.doldol.api.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/** docs/api.md 의 에러 코드 표와 1:1. 응답의 code 는 enum 이름 그대로 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값을 확인해 주세요."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "현재 비밀번호가 맞지 않습니다."),
    INVALID_RESET_TOKEN(HttpStatus.BAD_REQUEST, "링크가 만료됐거나 이미 사용됐어요. 비밀번호 찾기를 다시 해 주세요."),
    INVALID_FILE(HttpStatus.BAD_REQUEST, "이미지 파일(jpg · png · webp · gif, 5MB 이하)만 올릴 수 있어요."),

    // 401, 403
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "로그인이 만료됐어요. 다시 로그인해 주세요."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // 404
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 주소를 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니 상품을 찾을 수 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다."),
    BANNER_NOT_FOUND(HttpStatus.NOT_FOUND, "배너를 찾을 수 없습니다."),
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "문의를 찾을 수 없습니다."),

    // 409
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    OUT_OF_STOCK(HttpStatus.CONFLICT, "재고가 부족합니다."),
    CART_EMPTY(HttpStatus.CONFLICT, "장바구니가 비어 있습니다."),
    ORDER_NOT_CANCELLABLE(HttpStatus.CONFLICT, "취소할 수 없는 주문입니다."),
    INVALID_STATUS_TRANSITION(HttpStatus.CONFLICT, "바꿀 수 없는 주문 상태입니다."),
    CANNOT_CHANGE_OWN_ROLE(HttpStatus.CONFLICT, "자기 자신의 권한은 바꿀 수 없습니다."),
    ORDER_NOT_PAYABLE(HttpStatus.CONFLICT, "결제할 수 없는 주문입니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.CONFLICT, "결제 금액이 주문 금액과 다릅니다."),
    PAYMENT_NOT_COMPLETED(HttpStatus.CONFLICT, "결제가 완료되지 않았습니다."),
    INQUIRY_ALREADY_ANSWERED(HttpStatus.CONFLICT, "답변이 달린 문의는 지울 수 없어요."),

    // 400 (관리자)
    INVALID_PERIOD(HttpStatus.BAD_REQUEST, "조회 기간을 확인해 주세요. (최대 1년)"),
    // 502
    PAYMENT_GATEWAY_ERROR(HttpStatus.BAD_GATEWAY, "결제사 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."),

    // 500
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");

    private final HttpStatus status;

    private final String message;

}