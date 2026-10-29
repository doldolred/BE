package com.doldol.api.payment;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * 나이스페이 START API (매뉴얼: github.com/nicepayments/nicepay-manual).
 * 인증: Authorization: Basic base64(clientId:secretKey). 결과는 resultCode "0000" 이 성공.
 */
@Slf4j
@Component
public class NicepayClient {

	private static final String SUCCESS = "0000";

	/** paidAt 예: 2026-10-23T14:05:10.000+0900 */
	private static final DateTimeFormatter PAID_AT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	private final PaymentProperties.Nicepay props;

	private final RestClient restClient;

	public NicepayClient(PaymentProperties properties) {
		this.props = properties.nicepay();
		String basic = Base64.getEncoder()
			.encodeToString((props.clientId() + ":" + props.secretKey()).getBytes(StandardCharsets.UTF_8));
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(Duration.ofSeconds(5));
		factory.setReadTimeout(Duration.ofSeconds(30));
		this.restClient = RestClient.builder()
			.baseUrl(props.apiUrl())
			.requestFactory(new BufferingClientHttpRequestFactory(factory)) // 본문 길이(Content-Length)를 붙여 보낸다
			.defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
			.build();
	}

	/** returnUrl 로 온 인증 결과 위변조 검사 — hex(sha256(authToken + clientId + amount + secretKey)) */
	public boolean verifyAuthSignature(String authToken, String amount, String signature) {
		if (signature == null) {
			return false;
		}
		String expected = sha256Hex(authToken + props.clientId() + amount + props.secretKey());
		// 시간 차로 서명을 알아내지 못하게 끝까지 비교
		return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
				signature.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * 승인 요청 — 이걸 불러야 실제로 돈이 나간다.
	 * 응답을 못 받으면(시간 초과 등) 승인됐는지 알 수 없으니 망취소를 걸고 실패로 끝낸다.
	 */
	public Approval approve(String tid, int amount, String orderId) {
		try {
			return restClient.post()
				.uri("/v1/payments/{tid}", tid)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("amount", amount))
				.retrieve()
				.body(Approval.class);
		}
		catch (RestClientException e) {
			log.error("나이스페이 승인 응답 실패 orderId={} tid={}", orderId, tid, e);
			netCancel(orderId);
			throw new BusinessException(ErrorCode.PAYMENT_GATEWAY_ERROR);
		}
	}

	/** 망취소 — 승인 결과를 모를 때. orderId 로 취소한다 */
	public void netCancel(String orderId) {
		try {
			Result result = restClient.post()
				.uri("/v1/payments/netcancel")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("orderId", orderId))
				.retrieve()
				.body(Result.class);
			log.warn("나이스페이 망취소 orderId={} result={}", orderId, result);
		}
		catch (RestClientException e) {
			log.error("나이스페이 망취소 실패 — 관리자 확인 필요 orderId={}", orderId, e);
		}
	}

	/** 승인된 tid 를 전액 취소(환불). 실패하면 예외 */
	public void cancel(String tid, String orderId, String reason) {
		cancel(tid, orderId, reason, null);
	}

	/** cancelAmt 만큼만 부분 취소 (V12, null 이면 전액). 실패하면 예외 */
	public void cancel(String tid, String orderId, String reason, Integer cancelAmt) {
		Map<String, Object> body = new java.util.LinkedHashMap<>();
		body.put("reason", reason);
		body.put("orderId", orderId);
		if (cancelAmt != null) {
			body.put("cancelAmt", cancelAmt);
		}
		Result result;
		try {
			result = restClient.post()
				.uri("/v1/payments/{tid}/cancel", tid)
				.contentType(MediaType.APPLICATION_JSON)
				.body(body)
				.retrieve()
				.body(Result.class);
		}
		catch (RestClientException e) {
			log.error("나이스페이 취소 요청 실패 orderId={} tid={}", orderId, tid, e);
			throw new BusinessException(ErrorCode.PAYMENT_GATEWAY_ERROR);
		}
		if (result == null || !SUCCESS.equals(result.resultCode())) {
			log.error("나이스페이 취소 거절 orderId={} tid={} result={}", orderId, tid, result);
			throw new BusinessException(ErrorCode.PAYMENT_GATEWAY_ERROR,
					"결제 취소에 실패했어요: " + (result == null ? "응답 없음" : result.resultMsg()));
		}
	}

	/** 승인 응답 (필요한 것만) */
	public record Approval(String resultCode, String resultMsg, String tid, String orderId, String status,
			Integer amount, String payMethod, String paidAt, String receiptUrl) {

		public boolean isPaid() {
			return SUCCESS.equals(resultCode) && "paid".equals(status);
		}

		/** "0" 이거나 형식이 다르면 지금 시각 */
		public Instant paidAtInstant() {
			try {
				return OffsetDateTime.parse(paidAt, PAID_AT).toInstant();
			}
			catch (RuntimeException e) {
				return Instant.now();
			}
		}

	}

	/** 취소 · 망취소 응답 */
	public record Result(String resultCode, String resultMsg, String tid, String orderId, String status) {
	}

	private static String sha256Hex(String value) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

}
