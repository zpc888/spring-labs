package com.example.demo.exception;

public class BusinessSecurityException extends RuntimeException {
    public BusinessSecurityException(String message) {
        super(message);
    }
}