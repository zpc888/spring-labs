package com.example.demo.controller;

import com.example.demo.model.LoginRequest;
import com.example.demo.model.LoginResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class LoginController {

    private final RedisTemplate<String, String> redisTemplate;

    public LoginController(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request,
                                               @RequestHeader("channel") String channel) {
        String cardNumber = request.getCardNumber();
        String password = request.getPassword();

        if (request.getCardType() == null) {
            throw new IllegalArgumentException("Card type cannot be null");
        }
        if (cardNumber == null || cardNumber.isEmpty()) {
            throw new IllegalArgumentException("Card number cannot be null or empty");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (channel == null || channel.isEmpty()) {
            throw new IllegalArgumentException("Channel cannot be null or empty");
        }

        String token = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set("cardType:" + token, request.getCardType().name());
        redisTemplate.opsForValue().set("cardNumber:" + token, cardNumber);
        redisTemplate.opsForValue().set("channel:" + token, channel);

        return ResponseEntity.ok(new LoginResponse(token));
    }
}