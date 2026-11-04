package com.example.bakeryshop.controller.order;

import com.example.bakeryshop.dto.order.PlaceOrderRequest;
import com.example.bakeryshop.model.order.Order;
import com.example.bakeryshop.service.order.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        Order saved = orderService.placeOrder(request.userId(), request.note());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
