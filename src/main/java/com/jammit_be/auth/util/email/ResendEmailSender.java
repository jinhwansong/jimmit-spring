package com.jammit_be.auth.util.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Profile("!local")
@RequiredArgsConstructor
public class ResendEmailSender implements EmailSender {

    private final WebClient defaultWebClient;

    @Value("${resend.api-key:}")
    private String apiKey;

    @Value("${resend.from-email:noreply@jammit.com}")
    private String fromEmail;

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    @Override
    public void sendEmail(String to, String subject, String content) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("RESEND_API_KEY 환경변수가 설정되지 않았습니다. Render 환경변수에 RESEND_API_KEY를 설정해주세요.");
        }
        
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("from", fromEmail);
            requestBody.put("to", new String[]{to});
            requestBody.put("subject", subject);
            requestBody.put("html", content);

            defaultWebClient.post()
                    .uri(RESEND_API_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Resend 이메일 전송 성공: to={}, subject={}", to, subject);
        } catch (Exception e) {
            log.error("Resend 이메일 전송 실패: to={}, subject={}, error={}", to, subject, e.getMessage());
            throw new RuntimeException("이메일 전송 실패: " + e.getMessage(), e);
        }
    }
}
