package com.techpg.hotelbooking.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentRequest(@NotBlank String method) {}
