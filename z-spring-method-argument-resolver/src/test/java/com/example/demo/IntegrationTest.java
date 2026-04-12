package com.example.demo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTest {

    private static int redisPort;
    private static RedisServer redisServer;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @BeforeAll
    static void startEmbeddedRedis() throws IOException {
        redisPort = findAvailablePort();
        redisServer = new RedisServer(redisPort);
        redisServer.start();
    }

    @AfterAll
    static void stopEmbeddedRedis() throws IOException {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    @BeforeEach
    void clearRedis() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> redisPort);
    }

    @Test
    void testLoginHappyPath() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("channel", "MOBILE");
        HttpEntity<String> request = new HttpEntity<>(
                "{\"cardType\":\"DEBIT_CARD\",\"cardNumber\":\"1234567890\",\"password\":\"password123\"}",
                headers
        );

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/login",
                HttpMethod.POST,
                request,
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        String token = (String) response.getBody().get("token");
        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals("DEBIT_CARD", redisTemplate.opsForValue().get("cardType:" + token));
        assertEquals("1234567890", redisTemplate.opsForValue().get("cardNumber:" + token));
        assertEquals("MOBILE", redisTemplate.opsForValue().get("channel:" + token));
    }

    @Test
    void testSwaggerUiEndpointAccessibleWithoutToken() {
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/swagger-ui.html",
                HttpMethod.GET,
                null,
                String.class
        );

        assertTrue(response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is3xxRedirection());
    }

    @Test
    void testApiDocsEndpointAccessibleWithoutToken() {
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/docs",
                HttpMethod.GET,
                null,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("openapi"));
    }

    @Test
    void testTransferHappyPath() {
        String token = loginAndGetToken("DEBIT_CARD", "1234567890", "password123", "MOBILE");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-access-token", token);
        HttpEntity<String> request = new HttpEntity<>("{\"fromAccount\":\"ACC001\",\"toAccount\":\"ACC002\"}", headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/transfer3",
                HttpMethod.POST,
                request,
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("DEBIT_CARD", response.getBody().get("cardType"));
        assertEquals("1234567890", response.getBody().get("cardNumber"));
        assertEquals("MOBILE", response.getBody().get("channel"));
        assertEquals("ACC001", response.getBody().get("fromAccount"));
        assertEquals("ACC002", response.getBody().get("toAccount"));
        assertEquals("Transfer initiated", response.getBody().get("status"));
    }

    @Test
    void testLoginMissingCardType() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("channel", "MOBILE");
        HttpEntity<String> request = new HttpEntity<>("{\"cardNumber\":\"1234567890\",\"password\":\"password123\"}", headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/login",
                HttpMethod.POST,
                request,
                Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Card type cannot be null", response.getBody().get("error"));
    }

    @Test
    void testLoginEmptyCardNumber() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("channel", "MOBILE");
        HttpEntity<String> request = new HttpEntity<>(
                "{\"cardType\":\"DEBIT_CARD\",\"cardNumber\":\"\",\"password\":\"password123\"}",
                headers
        );

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/login",
                HttpMethod.POST,
                request,
                Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Card number cannot be null or empty", response.getBody().get("error"));
    }

    @Test
    void testLoginEmptyPassword() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("channel", "MOBILE");
        HttpEntity<String> request = new HttpEntity<>(
                "{\"cardType\":\"DEBIT_CARD\",\"cardNumber\":\"1234567890\",\"password\":\"\"}",
                headers
        );

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/login",
                HttpMethod.POST,
                request,
                Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Password cannot be null or empty", response.getBody().get("error"));
    }

    @Test
    void testLoginEmptyChannel() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("channel", "");
        HttpEntity<String> request = new HttpEntity<>(
                "{\"cardType\":\"DEBIT_CARD\",\"cardNumber\":\"1234567890\",\"password\":\"password123\"}",
                headers
        );

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/login",
                HttpMethod.POST,
                request,
                Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Channel cannot be null or empty", response.getBody().get("error"));
    }

    @Test
    void testLoginMissingChannelHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(
                "{\"cardType\":\"DEBIT_CARD\",\"cardNumber\":\"1234567890\",\"password\":\"password123\"}",
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/login",
                HttpMethod.POST,
                request,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testLoginMalformedJsonBody() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("channel", "MOBILE");
        HttpEntity<String> request = new HttpEntity<>(
                "{\"cardType\":\"DEBIT_CARD\",\"cardNumber\":\"1234567890\",\"password\":\"password123\"",
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/login",
                HttpMethod.POST,
                request,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testTransferWithoutToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("{\"fromAccount\":\"ACC001\",\"toAccount\":\"ACC002\"}", headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/transfer3",
                HttpMethod.POST,
                request,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("{\"error\": \"Missing access token\"}", response.getBody());
    }

    @Test
    void testTransferInvalidToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-access-token", "invalid-token");
        HttpEntity<String> request = new HttpEntity<>("{\"fromAccount\":\"ACC001\",\"toAccount\":\"ACC002\"}", headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/transfer3",
                HttpMethod.POST,
                request,
                Map.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Invalid access token - card not found", response.getBody().get("error"));
    }

    @Test
    void testTransferEmptyFromAccount() {
        String token = loginAndGetToken("DEBIT_CARD", "1234567890", "password123", "MOBILE");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-access-token", token);
        HttpEntity<String> request = new HttpEntity<>("{\"fromAccount\":\"\",\"toAccount\":\"ACC002\"}", headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/transfer3",
                HttpMethod.POST,
                request,
                Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("From account cannot be null or empty", response.getBody().get("error"));
    }

    @Test
    void testTransferEmptyToAccount() {
        String token = loginAndGetToken("DEBIT_CARD", "1234567890", "password123", "MOBILE");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-access-token", token);
        HttpEntity<String> request = new HttpEntity<>("{\"fromAccount\":\"ACC001\",\"toAccount\":\"\"}", headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/transfer3",
                HttpMethod.POST,
                request,
                Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("To account cannot be null or empty", response.getBody().get("error"));
    }

    private String loginAndGetToken(String cardType, String cardNumber, String password, String channel) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("channel", channel);
        HttpEntity<String> request = new HttpEntity<>(
                String.format("{\"cardType\":\"%s\",\"cardNumber\":\"%s\",\"password\":\"%s\"}", cardType, cardNumber, password),
                headers
        );

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/login",
                HttpMethod.POST,
                request,
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        String token = (String) response.getBody().get("token");
        assertNotNull(token);
        assertFalse(token.isBlank());
        return token;
    }

    private static int findAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot find available port for embedded Redis", exception);
        }
    }
}

