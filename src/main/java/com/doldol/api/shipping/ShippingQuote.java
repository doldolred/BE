package com.doldol.api.shipping;

/**
 * 배송비 계산 결과.
 * baseFee: 기본 배송비 (상품 금액이 freeThreshold 이상이면 0) · extraFee: 제주 · 도서 추가 (areaName 지역)
 * fee = baseFee + extraFee
 */
public record ShippingQuote(int baseFee, int extraFee, String areaName, int fee, int freeThreshold) {

}
