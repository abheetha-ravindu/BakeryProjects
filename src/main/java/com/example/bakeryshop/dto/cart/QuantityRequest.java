package com.example.bakeryshop.dto.cart;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record QuantityRequest(
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 50, message = "Quantity cannot be more than 50")
        int quantity) {
}
