// src/main/java/com/example/moabackend.global.token.service/CoolSmsService.java
package com.example.moabackend.global.token.service;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;

@Service
@Slf4j
public class CoolSmsService {

    private DefaultMessageService messageService;

    @Value("${coolsms.api.key}")
    private String apiKey;
    @Value("${coolsms.api.secret}")
    private String apiSecret;
    @Value("${coolsms.api.sender-number}")
    private String senderNumber;

    @PostConstruct // ✅ Bean 생성 및 @Value 주입 후 CoolSMS SDK 초기화
    private void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
    }

    /**
     * CoolSMS를 사용하여 인증 코드를 SMS로 발송합니다.
     */
    public void sendVerificationSms(String phoneNumber, String code) {
        String receiver = phoneNumber.replaceAll("[^0-9]", "");
        String content = String.format("[앱이름] 회원가입 인증번호는 [%s]입니다. (유효시간 5분)", code);

        Message message = new Message();
        message.setFrom(senderNumber);
        message.setTo(receiver);
        message.setText(content);

        try {
            SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
            log.info("CoolSMS 발송 성공: {}", response.toString());

            if (response.getStatusCode() == null || !response.getStatusCode().equals("2000")) {
                throw new RuntimeException("CoolSMS 발송 응답 실패: " + response.getStatusMessage());
            }

        } catch (Exception e) {
            log.error("CoolSMS 발송 실패: {}", e.getMessage(), e);
            throw new RuntimeException("SMS 발송에 실패했습니다. 크레딧 및 발신 번호 등록을 확인해주세요.", e);
        }
    }
}