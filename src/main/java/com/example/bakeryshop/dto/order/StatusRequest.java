package com.example.bakeryshop.dto.order;

import com.example.bakeryshop.model.order.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(
        @NotNull(message = "Status is required")
        OrderStatus status) {
}
