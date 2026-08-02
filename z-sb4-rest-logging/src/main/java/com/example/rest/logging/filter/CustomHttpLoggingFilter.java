package com.example.rest.logging.filter;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class CustomHttpLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CustomHttpLoggingFilter.class);
    private static int CACHE_LIMIT = 512*1024;
    private static final Set<String> SENSITIVE_HEADERS = Set.of("authorization", "cookie", "set-cookie", "x-api-key");

    // Define URL path patterns to completely ignore
    private static final Set<String> EXCLUDED_URLS = Set.of(
            "/api/v1/files/upload/**",
            "/api/v1/files/download/**",
            "/actuator/**",
            "/favicon.ico"
    );

    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Spring auto-wires the default Jackson ObjectMapper bean
    public CustomHttpLoggingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return EXCLUDED_URLS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, CACHE_LIMIT);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            logHttpExchangeAsJson(requestWrapper, responseWrapper, duration);

            // Mandatory step to pass the response data downstream
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logHttpExchangeAsJson(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long duration) {
        // 1. Gather Request Headers (Scrub Sensitive Values)
        Map<String, String> cleanedHeaders = new HashMap<>();
        Collections.list(request.getHeaderNames()).forEach(headerName -> {
            String lowerCaseHeader = headerName.toLowerCase();
            String value = SENSITIVE_HEADERS.contains(lowerCaseHeader) ? "******" : request.getHeader(headerName);
            cleanedHeaders.put(headerName, value);
        });

        // 2. Parse Request and Response Payloads
        Object reqBodyParsed = parseBody(request.getContentAsByteArray(), request.getCharacterEncoding());
        Object resBodyParsed = parseBody(response.getContentAsByteArray(), response.getCharacterEncoding());

        // 3. Populate JSON Log Object
        HttpExchangeLog exchangeLog = new HttpExchangeLog(
                Instant.now().toString(),
                request.getMethod(),
                request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""),
                cleanedHeaders,
                reqBodyParsed,
                response.getStatus(),
                resBodyParsed,
                duration
        );

        // 4. Output as Structured JSON
        try {
            // Use writeValueAsString for production single-line JSON log aggregation engines (Splunk, ELK)
            String jsonLog = objectMapper.writeValueAsString(exchangeLog);
            log.info(jsonLog);
        } catch (Exception e) {
            log.error("Failed to serialize HTTP exchange log to JSON", e);
        }
    }

    private Object parseBody(byte[] buf, String characterEncoding) {
        if (buf == null || buf.length == 0) {
            return "";
        }
        try {
            String rawString = new String(buf, 0, buf.length, characterEncoding != null ? characterEncoding : "UTF-8");
            // If it is valid JSON, parse it as a map/list so it sits nicely nested within the final log JSON
            if (rawString.trim().startsWith("{") || rawString.trim().startsWith("[")) {
                return objectMapper.readTree(rawString);
            }
            return rawString;
        } catch (IOException e) {
            return "[Unparseable Body Content]";
        }
    }
}
