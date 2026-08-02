package com.example.rest.logging.client;

import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class ResilientClientLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ResilientClientLoggingInterceptor.class);
    private final ObjectMapper objectMapper;

    public ResilientClientLoggingInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // Exclude specific health/internal URLs from logging clutter
        if (request.getURI().getPath().contains("/actuator") || request.getURI().getPath().contains("/health")) {
            return execution.execute(request, body);
        }

        // Generate a unique Correlation ID to match Stage 1 and Stage 2 logs if a crash happens
        String correlationId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        // === STAGE 1: Log immediately BEFORE sending (Guarantees trace on crash) ===
        logStage(Map.of(
                "timestamp", Instant.now().toString(),
                "event", "OUTBOUND_REQUEST_SENT",
                "correlationId", correlationId,
                "method", request.getMethod().name(),
                "uri", request.getURI().toString()
        ));

        ClientHttpResponse response;
        try {
            response = execution.execute(request, body);
        } catch (IOException e) {
            // Log explicitly if the network call failed or crashed mid-flight
            logStage(Map.of(
                    "timestamp", Instant.now().toString(),
                    "event", "OUTBOUND_REQUEST_CRASHED",
                    "correlationId", correlationId,
                    "error", e.getMessage(),
                    "durationMs", (System.currentTimeMillis() - startTime)
            ));
            throw e;
        }

        // === STAGE 2: Log AFTER receiving the response ===
        long duration = System.currentTimeMillis() - startTime;
        String rawResBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);

        logStage(Map.of(
                "timestamp", Instant.now().toString(),
                "event", "OUTBOUND_RESPONSE_RECEIVED",
                "correlationId", correlationId,
                "uri", request.getURI().toString(),
                "responseStatus", response.getStatusCode().value(),
                "responseBody", parseJsonContent(rawResBody),
                "durationMs", duration
        ));

        return response;
    }

    private void logStage(Map<String, Object> logPayload) {
        try {
            log.info(objectMapper.writeValueAsString(logPayload));
        } catch (Exception e) {
            log.error("Failed writing structural log entry", e);
        }
    }

    private Object parseJsonContent(String raw) {
        if (raw == null || raw.isBlank()) return "";
        try {
            return objectMapper.readTree(raw);
        } catch (Exception e) {
            return raw;
        }
    }
}
