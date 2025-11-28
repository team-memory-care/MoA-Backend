package com.example.moabackend.domain.report.service.ai;

import reactor.core.publisher.Mono;

public interface OpenAiService {
    Mono<String> callOpenAi(String prompt);
}
