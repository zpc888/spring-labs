package com.example.rest.logging.filter;

import java.util.Map;

public record HttpExchangeLog(
        String timestamp,
        String traceId,
        String spanId,
        String method,
        String uri,
        Map<String, String> requestHeaders,
        Object requestBody,
        int responseStatus,
        Object responseBody,
        long durationMs
) {}