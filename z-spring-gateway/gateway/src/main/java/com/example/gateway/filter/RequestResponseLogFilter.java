package com.example.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import org.reactivestreams.Publisher;

@Slf4j
@Component
public class RequestResponseLogFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_ID_ATTR = "requestId";
    private static final int MAX_LOGGED_BODY_LENGTH = 4_096;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        exchange.getAttributes().put(REQUEST_ID_ATTR, requestId);

        ServerHttpRequest request = exchange.getRequest();
        log.info("[{}] Incoming request: {} {} ", requestId, request.getMethod(), request.getURI());
        log.info("[{}] Request headers: {}", requestId, request.getHeaders());
        
        Map<String, String> queryParams = request.getQueryParams().toSingleValueMap();
        if (!queryParams.isEmpty()) {
            log.info("[{}] Request parameters: {}", requestId, queryParams);
        }

        ServerHttpResponse response = exchange.getResponse();
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(response) {
            @Override
            public @NonNull Mono<Void> writeWith(@NonNull Publisher<? extends DataBuffer> body) {
                MediaType contentType = getDelegate().getHeaders().getContentType();
                if (!isLoggableContentType(contentType)) {
                    return super.writeWith(body);
                }

                return DataBufferUtils.join(Flux.from(body))
                        .flatMap(dataBuffer -> {
                            byte[] content = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(content);
                            DataBufferUtils.release(dataBuffer);

                            logResponseBody(requestId, getDelegate().getHeaders(), content);

                            return super.writeWith(Mono.just(getDelegate().bufferFactory().wrap(content)));
                        })
                        .switchIfEmpty(super.writeWith(Mono.empty()));
            }

            @Override
            public @NonNull Mono<Void> writeAndFlushWith(@NonNull Publisher<? extends Publisher<? extends DataBuffer>> body) {
                return writeWith(Flux.from(body).flatMapSequential(publisher -> publisher));
            }
        };

        ServerWebExchange exchangeWithDecoratedResponse = exchange.mutate().response(decoratedResponse).build();

        return decorateExchangeWithRequestBody(exchangeWithDecoratedResponse, requestId)
                .flatMap(decoratedExchange -> chain.filter(decoratedExchange))
                .then(Mono.fromRunnable(() -> {
                    log.info("[{}] Response status: {}", requestId, response.getStatusCode());
                    log.info("[{}] Response headers: {}", requestId, response.getHeaders());
                }));
    }

    private Mono<ServerWebExchange> decorateExchangeWithRequestBody(ServerWebExchange exchange, String requestId) {
        ServerHttpRequest request = exchange.getRequest();
        if (!shouldLogRequestBody(request)) {
            return Mono.just(exchange);
        }

        return DataBufferUtils.join(request.getBody())
                .map(dataBuffer -> {
                    byte[] content = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(content);
                    DataBufferUtils.release(dataBuffer);

                    logRequestBody(requestId, request.getHeaders(), content);

                    ServerHttpRequest decoratedRequest = new ServerHttpRequestDecorator(request) {
                        @Override
                        public @NonNull Flux<DataBuffer> getBody() {
                            return Flux.defer(() -> Mono.just(exchange.getResponse().bufferFactory().wrap(content)));
                        }
                    };
                    return exchange.mutate().request(decoratedRequest).build();
                })
                .switchIfEmpty(Mono.fromSupplier(() -> {
                    log.info("[{}] Request payload: <empty>", requestId);
                    return exchange;
                }));
    }

    private void logRequestBody(String requestId, HttpHeaders headers, byte[] content) {
        if (content.length == 0) {
            log.info("[{}] Request payload: <empty>", requestId);
            return;
        }

        Charset charset = headers.getContentType() != null && headers.getContentType().getCharset() != null
                ? headers.getContentType().getCharset()
                : StandardCharsets.UTF_8;
        String requestBody = new String(content, charset);

        if (requestBody.length() > MAX_LOGGED_BODY_LENGTH) {
            log.info("[{}] Request payload (truncated to {} chars): {}...", requestId, MAX_LOGGED_BODY_LENGTH,
                    requestBody.substring(0, MAX_LOGGED_BODY_LENGTH));
            return;
        }

        log.info("[{}] Request payload: {}", requestId, requestBody);
    }

    private void logResponseBody(String requestId, HttpHeaders headers, byte[] content) {
        if (content.length == 0) {
            log.info("[{}] Response payload: <empty>", requestId);
            return;
        }

        Charset charset = headers.getContentType() != null && headers.getContentType().getCharset() != null
                ? headers.getContentType().getCharset()
                : StandardCharsets.UTF_8;
        String responseBody = new String(content, charset);

        if (responseBody.length() > MAX_LOGGED_BODY_LENGTH) {
            log.info("[{}] Response payload (truncated to {} chars): {}...", requestId, MAX_LOGGED_BODY_LENGTH,
                    responseBody.substring(0, MAX_LOGGED_BODY_LENGTH));
            return;
        }

        log.info("[{}] Response payload: {}", requestId, responseBody);
    }

    private boolean isLoggableContentType(MediaType contentType) {
        if (contentType == null) {
            return true;
        }

        return MediaType.APPLICATION_JSON.includes(contentType)
                || MediaType.APPLICATION_XML.includes(contentType)
                || MediaType.TEXT_XML.includes(contentType)
                || MediaType.APPLICATION_FORM_URLENCODED.includes(contentType)
                || "text".equalsIgnoreCase(contentType.getType())
                || contentType.getSubtype().contains("json")
                || contentType.getSubtype().contains("xml");
    }

    private boolean shouldLogRequestBody(ServerHttpRequest request) {
        HttpMethod method = request.getMethod();
        if (method == null || !(HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method) || HttpMethod.PATCH.equals(method))) {
            return false;
        }

        return isLoggableContentType(request.getHeaders().getContentType());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
