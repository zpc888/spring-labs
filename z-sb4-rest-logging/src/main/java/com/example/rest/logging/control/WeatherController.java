package com.example.rest.logging.control;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class WeatherController {
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> credentials) {
        // Return a mock token object
        return Map.of(
                "status", "authenticated",
                "token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.dummyData"
        );
    }
}
