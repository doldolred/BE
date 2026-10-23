package com.doldol.api.payment;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	/** 나이스페이 인증 결과 — 브라우저가 form 으로 POST 한다(로그인 없음). 처리 후 FE 화면으로 302 */
	@PostMapping(value = "/nicepay/return", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public ResponseEntity<Void> nicepayReturn(@ModelAttribute NicepayReturnRequest request) {
		return ResponseEntity.status(HttpStatus.FOUND)
			.location(URI.create(paymentService.nicepayReturn(request)))
			.build();
	}

}
