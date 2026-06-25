package com.example.sb4jackson.controller;

import com.example.sb4jackson.model.TestModelV2;
import com.example.sb4jackson.model.TestModelV3;
import com.example.sb4jackson.model.TestModelV2a;
import com.example.sb4jackson.model.TestModelV3a;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test-api")
public class TestApiController {

    @PostMapping("/annotate-with-v2a")
    public TestModelV2a annotateWithV2a(@RequestBody TestModelV2a request) {
        return request;
    }

    @PostMapping("/annotate-with-v3")
    public TestModelV3 annotateWithV3(@RequestBody TestModelV3 request) {
        return request;
    }

    @PostMapping("/annotate-with-v2")
    public TestModelV2 annotateWithV2(@RequestBody TestModelV2 request) {
        return request;
    }

    @PostMapping("/annotate-with-v3a")
    public TestModelV3a annotateWithV3a(@RequestBody TestModelV3a request) {
        return request;
    }
}
