package com.example.bakeryshop.dto.cart;

import com.example.bakeryshop.model.cart.CartItem;
import java.math.BigDecimal;
import java.util.List;

public record CartResponse(Long userId, List<CartItem> items, BigDecimal total) {
}
