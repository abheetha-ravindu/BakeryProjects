package com.example.bakeryshop.repository;

import com.example.bakeryshop.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    void deleteByCustomerId(Long customerId);
}
