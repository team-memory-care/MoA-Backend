// src/main/java/com/example/moabackend.global.token.service/CoolSmsService.java
package com.example.moabackend.global.token.service;

import com.example.moabackend.global.code.GlobalErrorCode;
import com.example.moabackend.global.exception.CustomException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    @PostConstruct
    private void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
    }

    /**
     * CoolSMS를 사용하여 인증 코드를 SMS로 발송합니다.
     */
    public void sendVerificationSms(String phoneNumber, String code) {
        String receiver = phoneNumber.replaceAll("[^0-9]", "");
        String content = String.format("[MoA] 회원가입 인증번호는 [%s]입니다. (유효시간 5분이에요~ ⏰)", code);

        Message message = new Message();
        message.setFrom(senderNumber);
        message.setTo(receiver);
        message.setText(content);

        try {
            SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
            log.info("CoolSMS 발송 성공: {}", response.toString());

            if (response.getStatusCode() == null || !response.getStatusCode().equals("2000")) {
                log.error("CoolSMS 응답 실패 코드. Code: {}, Message: {}", response.getStatusCode(), response.getStatusMessage());
                throw new CustomException(GlobalErrorCode.BAD_GATEWAY);
            }

        } catch (Exception e) {
            log.error("CoolSMS 발송 실패: {}", e.getMessage(), e);
            throw new CustomException(GlobalErrorCode.BAD_GATEWAY);
        }
    }
}