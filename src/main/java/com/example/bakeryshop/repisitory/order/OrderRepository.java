package com.example.bakeryshop.repisitory.order;

import com.example.bakeryshop.model.order.Order;
import com.example.bakeryshop.model.order.OrderStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    List<Order> findByStatus(OrderStatus status);
}