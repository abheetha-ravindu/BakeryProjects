package com.example.bakeryshop.dto.payment;

import com.example.bakeryshop.model.payment.PaymentStatus;
import jakarta.validation.constraints.NotNull;

// Represents a payment status request payload.
public record PaymentStatusRequest(
        @NotNull(message = "Status is required")
        PaymentStatus status) {
}