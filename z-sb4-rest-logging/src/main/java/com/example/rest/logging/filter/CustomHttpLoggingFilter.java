package com.example.rest.logging.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Set;

@Component
public class CustomHttpLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CustomHttpLoggingFilter.class);
    private static final int CACHE_LIMIT = 512*1024;

    // Define headers that must be masked in logs
    private static final Set<String> SENSITIVE_HEADERS = Set.of("authorization", "cookie", "set-cookie", "x-api-key");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Wrap request and response to cache streams for safe reading
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, CACHE_LIMIT);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            // Let the request proceed through the application chain
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Log everything AFTER processing so request/response body caches are filled
            logHttpExchange(requestWrapper, responseWrapper, duration);

            // CRITICAL: Copy response body back to the original response stream
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logHttpExchange(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long duration) {
        StringBuilder logBuilder = new StringBuilder("\n=== HTTP EXCHANGE LOG ===\n");

        // 1. Log Request Info
        logBuilder.append(String.format("Request: %s %s\n", request.getMethod(), request.getRequestURI()));

        // 2. Log and Clean Request Headers
        logBuilder.append("Request Headers:\n");
        Collections.list(request.getHeaderNames()).forEach(headerName -> {
            String lowerCaseHeader = headerName.toLowerCase();
            String value = SENSITIVE_HEADERS.contains(lowerCaseHeader) ? "******" : request.getHeader(headerName);
            logBuilder.append(String.format("  %s: %s\n", headerName, value));
        });

        // 3. Log Request Body
        String requestBody = getPayload(request.getContentAsByteArray(), request.getCharacterEncoding());
        logBuilder.append(String.format("Request Body: %s\n", requestBody.isBlank() ? "[empty]" : requestBody));

        // 4. Log Response Info
        logBuilder.append(String.format("Response Status: %d (%s ms)\n", response.getStatus(), duration));

        // 5. Log Response Body
        String responseBody = getPayload(response.getContentAsByteArray(), response.getCharacterEncoding());
        logBuilder.append(String.format("Response Body: %s\n", responseBody.isBlank() ? "[empty]" : responseBody));
        logBuilder.append("=========================");

        log.info(logBuilder.toString());
    }

    private String getPayload(byte[] buf, String characterEncoding) {
        if (buf == null || buf.length == 0) {
            return "";
        }
        try {
            return new String(buf, 0, buf.length, characterEncoding != null ? characterEncoding : "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "[Unsupported Encoding]";
        }
    }
}

