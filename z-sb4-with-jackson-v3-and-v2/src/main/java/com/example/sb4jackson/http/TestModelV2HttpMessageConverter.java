package com.example.sb4jackson.http;

import com.example.sb4jackson.model.TestModelV2;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;

import java.io.IOException;

public class TestModelV2HttpMessageConverter extends AbstractHttpMessageConverter<TestModelV2> {

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    public TestModelV2HttpMessageConverter() {
        super(MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return TestModelV2.class.isAssignableFrom(clazz);
    }

    @Override
    protected TestModelV2 readInternal(Class<? extends TestModelV2> clazz, HttpInputMessage inputMessage) throws IOException {
        return objectMapper.readValue(inputMessage.getBody(), clazz);
    }

    @Override
    protected void writeInternal(TestModelV2 testModelV2, HttpOutputMessage outputMessage) throws IOException {
        objectMapper.writeValue(outputMessage.getBody(), testModelV2);
    }
}
