package com.example.moabackend.domain.report.service.ai;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.moabackend.domain.report.code.error.ReportErrorCode;
import com.example.moabackend.global.code.ErrorCode;
import com.example.moabackend.global.exception.CustomException;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiServiceImpl implements OpenAiService {
    private final WebClient openAiWebClient;

    @Override
    public Mono<String> callOpenAi(String prompt) {
        return openAiWebClient
                .post()
                .uri("/chat/completions")
                .bodyValue(Map.of(
                        "model", "gpt-4o-mini",
                        "messages", List.of(Map.of("role", "user", "content", prompt)),
                        "response_format", Map.of("type", "json_object")))
                .retrieve()
                .onStatus(status -> status.value() == 401,
                        clientResponse -> handleError(clientResponse, ReportErrorCode.OPEN_AI_UNAUTHORIZED,
                                "OpenAI Unauthorized Error"))
                .onStatus(status -> status.value() == 429,
                        clientResponse -> handleError(clientResponse, ReportErrorCode.OPEN_AI_RATE_LIMIT,
                                "OpenAI Rate Limit Error"))
                .onStatus(HttpStatusCode::is5xxServerError,
                        clientResponse -> handleError(clientResponse, ReportErrorCode.OPEN_AI_INTERNAL_ERROR,
                                "OpenAI Internal Error"))
                .bodyToMono(JsonNode.class)
                .map(node -> node.get("choices").get(0).get("message").get("content").asText());
    }

    private Mono<? extends Throwable> handleError(ClientResponse response, ErrorCode errorCode, String logPrefix) {
        return response.bodyToMono(String.class)
                .flatMap(errorBody -> {
                    log.error("{} : {}", logPrefix, errorBody);
                    return Mono.error(new CustomException(errorCode));
                });
    }
}