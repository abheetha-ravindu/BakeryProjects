package com.example.bakeryprojects.repository;

import com.example.bakeryprojects.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    void deleteByCustomerId(Long customerId);
}
