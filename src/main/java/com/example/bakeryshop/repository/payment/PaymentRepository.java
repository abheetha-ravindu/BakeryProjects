package com.example.bakeryshop.repository.payment;

import com.example.bakeryshop.model.payment.Payment;
import com.example.bakeryshop.model.payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByOrderId(Long orderId);

    List<Payment> findByOrderIdIn(List<Long> orderIds);

    boolean existsByOrderIdAndStatus(Long orderId, PaymentStatus status);
}