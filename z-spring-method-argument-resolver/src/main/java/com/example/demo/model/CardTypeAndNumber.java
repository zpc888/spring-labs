package com.example.demo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CardTypeAndNumber(
        @NotNull @NotBlank CardType cardType,
        @NotNull @NotBlank String cardNumber) {
}