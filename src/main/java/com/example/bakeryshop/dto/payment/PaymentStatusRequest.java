package com.example.bakeryshop.dto.payment;

import com.example.bakeryshop.model.payment.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record PaymentStatusRequest(
        @NotNull(message = "Status is required")
        PaymentStatus status) {
}