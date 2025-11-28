package com.example.moabackend.global.constant;

public class RedisKey {
    public static final String REFRESH_TOKEN_PREFIX = "user:refresh_token:";

    // redis stream key
    public static final String REPORT_STREAM_KEY = "report-stream";
    public static final String REPORT_GROUP = "report-group";
    public static final String REPORT_CONSUMER = "report-worker";
    public static final String REPORT_MESSAGE_MAP_KEY = "report-payload";
    public static final String REPORT_RETRY_COUNT = "report-retry-count";
    public static final String REPORT_DLQ_STREAM_KEY = "report-dlq-stream";
    public static final long REDIS_STREAM_MAX_LEN = 1500;
}
