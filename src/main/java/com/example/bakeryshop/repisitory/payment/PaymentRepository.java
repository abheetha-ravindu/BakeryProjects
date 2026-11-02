package com.example.bakeryshop.repisitory.payment;

import com.example.bakeryshop.model.payment.Payment;
import com.example.bakeryshop.model.payment.PaymentStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByOrderId(Long orderId);

    List<Payment> findByOrderIdIn(List<Long> orderIds);

    boolean existsByOrderIdAndStatus(Long orderId, PaymentStatus status);
}