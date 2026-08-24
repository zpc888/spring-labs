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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RestClientLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RestClientLoggingInterceptor.class);
    private static final Set<String> SENSITIVE_HEADERS = Set.of("authorization", "cookie", "x-api-key");

    private final ObjectMapper objectMapper;

    public RestClientLoggingInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        long startTime = System.currentTimeMillis();

        // Execute the call and wrapper the response to buffer the body stream safely
        ClientHttpResponse response = execution.execute(request, body);
        long duration = System.currentTimeMillis() - startTime;

        logOutboundExchange(request, body, response, duration);

        return response;
    }

    private void logOutboundExchange(HttpRequest request, byte[] reqBody, ClientHttpResponse response, long duration) throws IOException {
        // 1. Scrub Headers
        Map<String, String> cleanedHeaders = new HashMap<>();
        request.getHeaders().forEach((name, values) -> {
            String value = SENSITIVE_HEADERS.contains(name.toLowerCase()) ? "******" : String.join(", ", values);
            cleanedHeaders.put(name, value);
        });

        // 2. Parse Request and Response Bodies
        Object reqBodyParsed = parseBody(new String(reqBody, StandardCharsets.UTF_8));

        // Read response safely via stream copy (requires BufferingClientHttpRequestFactory configured below)
        String rawResBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        Object resBodyParsed = parseBody(rawResBody);

        // 3. Map to Structured Object
        Map<String, Object> logPayload = new LinkedHashMap<>();
        logPayload.put("timestamp", Instant.now().toString());
        logPayload.put("traceId", MDC.get("traceId"));
        logPayload.put("spanId", MDC.get("spanId"));
        logPayload.put("direction", "OUTBOUND");
        logPayload.put("method", request.getMethod().name());
        logPayload.put("uri", request.getURI().toString());
        logPayload.put("requestHeaders", cleanedHeaders);
        logPayload.put("requestBody", reqBodyParsed);
        logPayload.put("responseStatus", response.getStatusCode().value());
        logPayload.put("responseBody", resBodyParsed);
        logPayload.put("durationMs", duration);

        // 4. Print structured JSON string
        try {
            log.info(objectMapper.writeValueAsString(logPayload));
        } catch (Exception e) {
            log.error("Failed to serialize outbound log", e);
        }
    }

    private Object parseBody(String rawBody) {
        if (rawBody == null || rawBody.isBlank()) return "";
        try {
            if (rawBody.trim().startsWith("{") || rawBody.trim().startsWith("[")) {
                return objectMapper.readTree(rawBody);
            }
            return rawBody;
        } catch (Exception e) {
            return "[Unparseable Body]";
        }
    }
}
