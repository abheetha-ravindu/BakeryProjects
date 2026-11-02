package com.example.bakeryshop.dto;
import com.example.bakeryshop.model.CartItem;
import java.math.BigDecimal;
import java.util.List;

public record CartResponse(Long userId, List<CartItem> items, BigDecimal total) {
}