package com.example.moabackend.domain.report.entity.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EReportType {
    DAILY("daily"),
    WEEKLY("weekly"),
    MONTHLY("monthly");

    private final String value;

    @JsonCreator
    public static EReportType from(String value) {
        if (value == null) return null;

        for (EReportType type : EReportType.values()) {
            if (type.value.equalsIgnoreCase(value)) { // 대소문자 구분 없이 매칭
                return type;
            }
        }

        throw new IllegalArgumentException("Invalid reportType: " + value);
    }
}
