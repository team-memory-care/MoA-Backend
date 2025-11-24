package com.example.moabackend.global.util;

import com.example.moabackend.global.constant.Constants;

public final class S3UrlUtils {

    private S3UrlUtils() {
    }

    /**
     * S3 URI/Key를 클라이언트가 사용할 수 있는 HTTPS URL로 변환합니다.
     * DB에 저장된 's3://bucket/key' 형태나 순수 'key' 형태 모두 처리합니다.
     * * @param rawKey DB에서 조회된 원본 경로 문자열
     * @return HTTPS URL 또는 빈 문자열
     */
    public static String convertToHttpUrl(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) return "";

        // 1. 이미 http로 시작하면 변환 없이 반환
        if (rawKey.startsWith("http")) return rawKey;

        // 2. s3://... 형태라면 파싱해서 파일명만 추출
        if (rawKey.startsWith("s3://")) {
            // "s3://" (5글자) 이후의 첫 슬래시를 찾아 그 뒤를 파일명으로 간주
            int slashIndex = rawKey.indexOf("/", 5);
            if (slashIndex != -1) {
                rawKey = rawKey.substring(slashIndex + 1);
            }
        }

        // 3. 최종 조립: 안전한 프리픽스 + 파일명
        return Constants.S3_URL_PREFIX + rawKey; //
    }
}