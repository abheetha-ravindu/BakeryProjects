package com.example.bakeryshop.repisitory;

import com.example.bakeryshop.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
// Accesses Delivery persistence logic.

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    void deleteByCustomerId(Long customerId);
}
