package com.doldol.api.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 메일 보내기 (SMTP — AWS SES · 네이버웍스 등 아무 SMTP).
 * SPRING_MAIL_HOST 가 없거나 비어 있으면 보내지 않고 로그에만 남긴다 (로컬 개발).
 */
@Slf4j
@Service
public class MailService {

    private final ObjectProvider<JavaMailSender> mailSender;

    private final String from;

    /** 운영 compose 는 값이 없으면 빈 문자열로 넘기므로, 비어 있으면 설정 안 한 것으로 본다 */
    private final boolean configured;

    public MailService(ObjectProvider<JavaMailSender> mailSender, @Value("${app.mail.from}") String from,
            @Value("${spring.mail.host:}") String host) {
        this.mailSender = mailSender;
        this.from = from;
        this.configured = !host.isBlank();
    }

    /** 실패해도 예외를 던지지 않는다 — 보냈으면 true */
    public boolean send(String to, String subject, String text) {
        JavaMailSender sender = configured ? mailSender.getIfAvailable() : null;
        if (sender == null) {
            log.warn("[메일 설정 없음 — 보내지 않음] to={} subject={}\n{}", to, subject, text);
            return false;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            sender.send(message);
            return true;
        }
        catch (MailException e) {
            log.error("메일 발송 실패 to={} subject={}", to, subject, e);
            return false;
        }
    }

}
