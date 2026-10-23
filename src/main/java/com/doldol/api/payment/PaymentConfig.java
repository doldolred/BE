package com.doldol.api.payment;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** 결제 설정 읽기 + 결제 대기 주문 자동 취소(@Scheduled) 켜기 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(PaymentProperties.class)
public class PaymentConfig {

}
