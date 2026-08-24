package com.example.rest.logging.client;

import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
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
        Map<String, Object> requestLog = new LinkedHashMap<>();
        requestLog.put("timestamp", Instant.now().toString());
        requestLog.put("traceId", MDC.get("traceId"));
        requestLog.put("spanId", MDC.get("spanId"));
        requestLog.put("event", "OUTBOUND_REQUEST_SENT");
        requestLog.put("correlationId", correlationId);
        requestLog.put("method", request.getMethod().name());
        requestLog.put("uri", request.getURI().toString());
        logStage(requestLog);

        ClientHttpResponse response;
        try {
            response = execution.execute(request, body);
        } catch (IOException e) {
            // Log explicitly if the network call failed or crashed mid-flight
            Map<String, Object> crashLog = new LinkedHashMap<>();
            crashLog.put("timestamp", Instant.now().toString());
            crashLog.put("traceId", MDC.get("traceId"));
            crashLog.put("spanId", MDC.get("spanId"));
            crashLog.put("event", "OUTBOUND_REQUEST_CRASHED");
            crashLog.put("correlationId", correlationId);
            crashLog.put("error", e.getMessage());
            crashLog.put("durationMs", (System.currentTimeMillis() - startTime));
            logStage(crashLog);
            throw e;
        }

        // === STAGE 2: Log AFTER receiving the response ===
        long duration = System.currentTimeMillis() - startTime;
        String rawResBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);

        Map<String, Object> responseLog = new LinkedHashMap<>();
        responseLog.put("timestamp", Instant.now().toString());
        responseLog.put("traceId", MDC.get("traceId"));
        responseLog.put("spanId", MDC.get("spanId"));
        responseLog.put("event", "OUTBOUND_RESPONSE_RECEIVED");
        responseLog.put("correlationId", correlationId);
        responseLog.put("uri", request.getURI().toString());
        responseLog.put("responseStatus", response.getStatusCode().value());
        responseLog.put("responseBody", parseJsonContent(rawResBody));
        responseLog.put("durationMs", duration);
        logStage(responseLog);

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
