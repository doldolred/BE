package com.doldol.api.payment;

/** 나이스페이가 returnUrl 로 form POST 하는 값 (이름이 그대로 form 필드 이름) */
public record NicepayReturnRequest(String authResultCode, String authResultMsg, String tid, String clientId,
		String orderId, String amount, String mallReserved, String authToken, String signature) {

}
