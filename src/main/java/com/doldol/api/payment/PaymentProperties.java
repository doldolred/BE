package com.doldol.api.payment;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** application.yml 의 app.payment.* */
@ConfigurationProperties(prefix = "app.payment")
public record PaymentProperties(String frontendUrl, Duration pendingTimeout, Nicepay nicepay) {

	public record Nicepay(String clientId, String secretKey, String apiUrl) {
	}

}
