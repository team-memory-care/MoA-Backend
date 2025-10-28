// src/main/java/com/example/moabackend/global/token/service/TwilioVerificationService.java
package com.example.moabackend.global.token.service;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioVerificationService {

    private final String accountSid;
    private final String authToken;
    private final String serviceSid;

    private static final String DEFAULT_COUNTRY_CODE = "KR";

    public TwilioVerificationService(
            @Value("${sms.service.account-sid}") String accountSid,
            @Value("${sms.service.auth-token}") String authToken,
            @Value("${sms.service.verify-sid}") String serviceSid) {

        this.accountSid = accountSid;
        this.authToken = authToken;
        this.serviceSid = serviceSid;

        Twilio.init(accountSid, authToken);
    }

    /**
     * [Twilio API 호출] 인증 번호 발송을 요청합니다.
     */
    public void requestVerificationCode(String phoneNumber) {
        // ✅ normalizeToE164WithLib 메서드를 호출하도록 수정
        String e164FormatPhoneNumber = normalizeToE164WithLib(phoneNumber);

        try {
            Verification verification = Verification.creator(
                            serviceSid,
                            e164FormatPhoneNumber,
                            "sms")
                    .create();

            if (!verification.getStatus().equals("pending")) {
                throw new RuntimeException("인증 코드 발송 요청 실패: " + verification.getStatus());
            }
        } catch (Exception e) {
            throw new RuntimeException("Twilio API 호출 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * [Twilio API 호출] 사용자 입력 코드를 검증합니다.
     */
    public boolean checkVerificationCode(String phoneNumber, String code) {
        // ✅ normalizeToE164WithLib 메서드를 호출하도록 수정
        String e164FormatPhoneNumber = normalizeToE164WithLib(phoneNumber);

        try {
            VerificationCheck verificationCheck = VerificationCheck.creator(
                            serviceSid)
                    .setTo(e164FormatPhoneNumber)
                    .setCode(code)
                    .create();

            return verificationCheck.getStatus().equals("approved");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Google libphonenumber를 사용하여 전화번호를 E.164 포맷으로 변환합니다.
     */
    private String normalizeToE164WithLib(String phoneNumber) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber number = phoneUtil.parse(phoneNumber, DEFAULT_COUNTRY_CODE);

            if (phoneUtil.isValidNumber(number)) {
                return phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164);
            }
        } catch (Exception e) {
            throw new RuntimeException("전화번호 포맷 오류: " + e.getMessage());
        }
        throw new IllegalArgumentException("유효하지 않거나 변환할 수 없는 전화번호입니다.");
    }
}