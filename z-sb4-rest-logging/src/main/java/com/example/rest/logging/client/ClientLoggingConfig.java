package com.example.rest.logging.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import tools.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ClientLoggingConfig {

    private final ObjectMapper objectMapper;
    private final String httpBinUrl;

    public ClientLoggingConfig(ObjectMapper objectMapper, @Value("${app.integration.httpbin.url}") String httpBinUrl) {
        this.objectMapper = objectMapper;
        this.httpBinUrl = httpBinUrl;
    }

    @Bean
    public ClientHttpRequestInterceptor loggingInterceptor() {
//        return new RestClientLoggingInterceptor(objectMapper);
        return new ResilientClientLoggingInterceptor(objectMapper);
    }

    // Option A: Configuration for modern RestClient (Spring Boot 3.2+)
    @Bean
    public RestClient restClient(RestClient.Builder builder, ClientHttpRequestInterceptor loggingInterceptor) {
        return builder
                .baseUrl(httpBinUrl)
                // CRITICAL: Enables response stream re-read capabilities
                .requestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
                .requestInterceptor(loggingInterceptor)
                .build();
    }

    // Option B: Configuration for classic RestTemplate
    @Bean
    public RestTemplate restTemplate(ClientHttpRequestInterceptor loggingInterceptor) {
        RestTemplate restTemplate = new RestTemplate(
                new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory())
        );
        restTemplate.getInterceptors().add(loggingInterceptor);
        return restTemplate;
    }
}
